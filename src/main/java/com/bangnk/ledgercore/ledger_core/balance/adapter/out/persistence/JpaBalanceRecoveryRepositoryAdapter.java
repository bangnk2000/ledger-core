package com.bangnk.ledgercore.ledger_core.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceRecoveryRepositoryPort;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaBalanceRecoveryRepositoryAdapter implements BalanceRecoveryRepositoryPort {

	private final BalanceRebuildJobJpaRepository rebuildJobRepository;
	private final BalanceRebuildCheckpointJpaRepository checkpointRepository;
	private final BalanceReconciliationRecordJpaRepository reconciliationRepository;

	public JpaBalanceRecoveryRepositoryAdapter(
			BalanceRebuildJobJpaRepository rebuildJobRepository,
			BalanceRebuildCheckpointJpaRepository checkpointRepository,
			BalanceReconciliationRecordJpaRepository reconciliationRepository
	) {
		this.rebuildJobRepository = rebuildJobRepository;
		this.checkpointRepository = checkpointRepository;
		this.reconciliationRepository = reconciliationRepository;
	}

	@Override
	public RebuildJobRecord saveRebuildJob(RebuildJobRecord job) {
		rebuildJobRepository.save(new BalanceRebuildJobJpaEntity(
			job.jobId(), job.scope(), job.replayContractVersion(), job.status(), job.checkpointToken(), job.processedRecordCount(),
			job.startedAt(), job.finishedAt(), job.requestedBy(), job.failureReason()));
		return job;
	}

	@Override
	public Optional<RebuildJobRecord> findRebuildJob(UUID jobId) {
		return rebuildJobRepository.findById(jobId).map(entity -> new RebuildJobRecord(
			entity.getJobId(), entity.getScope(), entity.getReplayContractVersion(), entity.getStatus(), entity.getCheckpointToken(),
			entity.getProcessedRecordCount(), entity.getStartedAt(), entity.getFinishedAt(), entity.getRequestedBy(), entity.getFailureReason()));
	}

	@Override
	public RebuildCheckpointRecord saveCheckpoint(RebuildCheckpointRecord checkpoint) {
		checkpointRepository.save(new BalanceRebuildCheckpointJpaEntity(
			checkpoint.checkpointId(), checkpoint.jobId(), checkpoint.accountId(), checkpoint.ledgerSequence(), checkpoint.reservationSequence(),
			checkpoint.createdAt()));
		return checkpoint;
	}

	@Override
	public Optional<RebuildCheckpointRecord> findLatestCheckpoint(UUID jobId) {
		return checkpointRepository.findTopByJobIdOrderByCreatedAtDesc(jobId).map(entity -> new RebuildCheckpointRecord(
			entity.getCheckpointId(), entity.getJobId(), entity.getAccountId(), entity.getLedgerSequence(), entity.getReservationSequence(),
			entity.getCreatedAt()));
	}

	@Override
	public ReconciliationRecord saveReconciliationRecord(ReconciliationRecord record) {
		reconciliationRepository.save(new BalanceReconciliationRecordJpaEntity(
			record.reconciliationId(), record.accountScope(), record.expectedBalance(), record.actualBalance(), record.differenceSummary(),
			record.severity(), record.status(), record.investigationReference(), record.createdAt(), record.updatedAt()));
		return record;
	}
}
