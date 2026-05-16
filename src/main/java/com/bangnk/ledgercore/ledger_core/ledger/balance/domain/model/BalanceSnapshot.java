package com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.BalanceStateRepositoryPort.BalanceStateRecord;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.ReconciliationStatus;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.MoneyAmount;
import java.time.Instant;
import java.util.UUID;

public record BalanceSnapshot(
		UUID snapshotId,
		AccountId accountId,
		CurrencyCode currency,
		MoneyAmount ledgerBalance,
		MoneyAmount lockedAmount,
		MoneyAmount pendingDebitAmount,
		MoneyAmount pendingCreditAmount,
		MoneyAmount availableBalance,
		long snapshotVersion,
		long asOfSequence,
		Instant asOfTime,
		ConsistencyMode consistencyMode,
		ReconciliationStatus reconciliationStatus,
		Instant createdAt
) {
	public enum ConsistencyMode {
		STRONG,
		DERIVED
	}

	public static BalanceSnapshot fromState(BalanceStateRecord state, ConsistencyMode consistencyMode, Instant now) {
		return new BalanceSnapshot(
			UUID.randomUUID(),
			state.key().accountId(),
			state.key().currency(),
			state.ledgerBalance(),
			state.lockedAmount(),
			state.pendingDebitAmount(),
			state.pendingCreditAmount(),
			state.availableBalance(),
			state.version(),
			state.ledgerAsOfSequence(),
			state.updatedAt(),
			consistencyMode,
			state.reconciliationStatus(),
			now);
	}

	public static BalanceSnapshot empty(AccountId accountId, CurrencyCode currency, ConsistencyMode consistencyMode, Instant now) {
		return new BalanceSnapshot(
			UUID.randomUUID(),
			accountId,
			currency,
			MoneyAmount.zero(),
			MoneyAmount.zero(),
			MoneyAmount.zero(),
			MoneyAmount.zero(),
			MoneyAmount.zero(),
			0L,
			0L,
			now,
			consistencyMode,
			ReconciliationStatus.HEALTHY,
			now);
	}
}
