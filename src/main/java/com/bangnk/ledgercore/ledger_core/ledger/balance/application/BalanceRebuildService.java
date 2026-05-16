package com.bangnk.ledgercore.ledger_core.ledger.balance.application;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in.BalanceRebuildUseCase;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.BalanceRecoveryRepositoryPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.LedgerReplayExportPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.LedgerReplayExportPort.ReplayBatch;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.LedgerReplayExportPort.ReplayCursor;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceRebuildCheckpoint;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceRebuildJob;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BalanceRebuildService implements BalanceRebuildUseCase {

	private final BalanceRecoveryRepositoryPort recoveryRepository;
	private final LedgerReplayExportPort replayExportPort;
	private final BalanceReplayOrderingService replayOrderingService;
	private final BalanceObservability observability;
	private final Clock clock;

	public BalanceRebuildService(
			BalanceRecoveryRepositoryPort recoveryRepository,
			LedgerReplayExportPort replayExportPort,
			BalanceReplayOrderingService replayOrderingService,
			BalanceObservability observability,
			Clock clock
	) {
		this.recoveryRepository = recoveryRepository;
		this.replayExportPort = replayExportPort;
		this.replayOrderingService = replayOrderingService;
		this.observability = observability;
		this.clock = clock;
	}

	@Override
	public BalanceRebuildJob start(StartRebuildCommand command) {
		Instant now = Instant.now(clock);
		BalanceRebuildJob running = BalanceRebuildJob.start(command.scope(), command.replayContractVersion(), command.requestedBy(), now);
		recoveryRepository.saveRebuildJob(running.toRecord());

		ReplayBatch batch = replayExportPort.export(new ReplayCursor(0L, null), 1_000);
		var ordered = replayOrderingService.orderDeterministically(batch.records());
		long processed = ordered.size();
		String checkpointToken = batch.nextCursor() != null ? batch.nextCursor().checkpointToken() : null;
		long ledgerSequence = batch.nextCursor() != null ? batch.nextCursor().ledgerSequence() : 0L;
		String checkpointAccount = ordered.isEmpty() ? null : ordered.getLast().accountId().value();
		recoveryRepository.saveCheckpoint(BalanceRebuildCheckpoint.create(running.jobId(), checkpointAccount, ledgerSequence, 0L, now).toRecord());

		BalanceRebuildJob completed = running.complete(checkpointToken, processed, Instant.now(clock));
		recoveryRepository.saveRebuildJob(completed.toRecord());
		observability.recordRebuildProgress(processed);
		return completed;
	}

	@Override
	public Optional<BalanceRebuildJob> get(UUID jobId) {
		return recoveryRepository.findRebuildJob(jobId).map(BalanceRebuildJob::fromRecord);
	}
}
