package com.bangnk.ledgercore.ledger_core.ledger.balance.application;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in.BalanceReconciliationUseCase;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.BalanceRecoveryRepositoryPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.BalanceStateRepositoryPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.ReconciliationSeverity;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceReconciliationRecord;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BalanceReconciliationService implements BalanceReconciliationUseCase {

	private final BalanceStateRepositoryPort balanceStateRepositoryPort;
	private final BalanceRecoveryRepositoryPort recoveryRepository;
	private final BalanceObservability observability;
	private final Clock clock;

	public BalanceReconciliationService(
			BalanceStateRepositoryPort balanceStateRepositoryPort,
			BalanceRecoveryRepositoryPort recoveryRepository,
			BalanceObservability observability,
			Clock clock
	) {
		this.balanceStateRepositoryPort = balanceStateRepositoryPort;
		this.recoveryRepository = recoveryRepository;
		this.observability = observability;
		this.clock = clock;
	}

	@Override
	public ReconciliationAccepted start(StartReconciliationCommand command) {
		UUID runId = UUID.randomUUID();
		Instant now = Instant.now(clock);
		balanceStateRepositoryPort.findByAccountAndCurrency(
			new com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId(command.scope()),
			new com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode("USD"))
			.ifPresent(state -> {
				if (state.availableBalance().isNegative()) {
					BalanceReconciliationRecord drift = BalanceReconciliationRecord.open(
						command.scope(),
						"{\"availableBalance\":\">=0\"}",
						"{\"availableBalance\":\"" + state.availableBalance().value().toPlainString() + "\"}",
						"{\"reason\":\"negative_available_balance\"}",
						ReconciliationSeverity.CRITICAL,
						now);
					recoveryRepository.saveReconciliationRecord(drift.toRecord());
					observability.recordReconciliationDiscrepancy("critical");
				}
			});
		return new ReconciliationAccepted(runId, "ACCEPTED");
	}
}
