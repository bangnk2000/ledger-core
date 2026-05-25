package com.bangnk.ledgercore.ledger_core.balance.domain.model;

import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceRecoveryRepositoryPort.ReconciliationRecord;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReconciliationRecordStatus;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReconciliationSeverity;
import java.time.Instant;
import java.util.UUID;

public record BalanceReconciliationRecord(
		UUID reconciliationId,
		String accountScope,
		String expectedBalance,
		String actualBalance,
		String differenceSummary,
		ReconciliationSeverity severity,
		ReconciliationRecordStatus status,
		String investigationReference,
		Instant createdAt,
		Instant updatedAt
) {
	public static BalanceReconciliationRecord open(
			String accountScope,
			String expectedBalance,
			String actualBalance,
			String differenceSummary,
			ReconciliationSeverity severity,
			Instant now
	) {
		return new BalanceReconciliationRecord(
			UUID.randomUUID(),
			accountScope,
			expectedBalance,
			actualBalance,
			differenceSummary,
			severity,
			ReconciliationRecordStatus.OPEN,
			null,
			now,
			now);
	}

	public static BalanceReconciliationRecord fromRecord(ReconciliationRecord record) {
		return new BalanceReconciliationRecord(
			record.reconciliationId(),
			record.accountScope(),
			record.expectedBalance(),
			record.actualBalance(),
			record.differenceSummary(),
			record.severity(),
			record.status(),
			record.investigationReference(),
			record.createdAt(),
			record.updatedAt());
	}

	public ReconciliationRecord toRecord() {
		return new ReconciliationRecord(
			reconciliationId,
			accountScope,
			expectedBalance,
			actualBalance,
			differenceSummary,
			severity,
			status,
			investigationReference,
			createdAt,
			updatedAt);
	}
}
