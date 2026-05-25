package com.bangnk.ledgercore.ledger_core.balance.application.port.in;

import java.util.UUID;

public interface BalanceReconciliationUseCase {

	ReconciliationAccepted start(StartReconciliationCommand command);

	record StartReconciliationCommand(String scope, boolean rebuildIfDriftDetected) {
	}

	record ReconciliationAccepted(UUID reconciliationRunId, String status) {
	}
}
