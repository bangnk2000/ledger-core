package com.bangnk.ledgercore.ledger_core.balance.domain.model;

import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceStateRepositoryPort.BalanceStateKey;
import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceStateRepositoryPort.BalanceStateRecord;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReconciliationStatus;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import java.time.Instant;

public record BalanceState(
		BalanceStateKey key,
		MoneyAmount ledgerBalance,
		MoneyAmount lockedAmount,
		MoneyAmount pendingDebitAmount,
		MoneyAmount pendingCreditAmount,
		long version,
		long ledgerAsOfSequence,
		long reservationAsOfSequence,
		ReconciliationStatus reconciliationStatus,
		Instant updatedAt
) {
	public static BalanceState empty(AccountId accountId, CurrencyCode currency) {
		return new BalanceState(
			new BalanceStateKey(accountId, currency),
			MoneyAmount.zero(),
			MoneyAmount.zero(),
			MoneyAmount.zero(),
			MoneyAmount.zero(),
			0L,
			0L,
			0L,
			ReconciliationStatus.HEALTHY,
			Instant.now());
	}

	public static BalanceState fromRecord(BalanceStateRecord record) {
		return new BalanceState(
			record.key(),
			record.ledgerBalance(),
			record.lockedAmount(),
			record.pendingDebitAmount(),
			record.pendingCreditAmount(),
			record.version(),
			record.ledgerAsOfSequence(),
			record.reservationAsOfSequence(),
			record.reconciliationStatus(),
			record.updatedAt());
	}

	public MoneyAmount availableBalance() {
		return ledgerBalance.subtract(lockedAmount).subtract(pendingDebitAmount).add(pendingCreditAmount);
	}

	public BalanceState reserve(BalanceDirection direction, MoneyAmount amount, Instant now) {
		MoneyAmount nextLocked = switch (direction) {
			case DEBIT -> lockedAmount.add(amount);
			case CREDIT -> lockedAmount;
		};
		BalanceState next = new BalanceState(
			key,
			ledgerBalance,
			nextLocked,
			pendingDebitAmount,
			pendingCreditAmount,
			version + 1,
			ledgerAsOfSequence,
			reservationAsOfSequence + 1,
			reconciliationStatus,
			now);
		if (next.availableBalance().isNegative()) {
			throw new IllegalArgumentException("Insufficient available balance");
		}
		return next;
	}

	public BalanceState confirm(BalanceDirection direction, MoneyAmount amount, Instant now) {
		MoneyAmount nextLedger = switch (direction) {
			case DEBIT -> ledgerBalance.subtract(amount);
			case CREDIT -> ledgerBalance.add(amount);
		};
		MoneyAmount nextLocked = switch (direction) {
			case DEBIT -> lockedAmount.subtract(amount);
			case CREDIT -> lockedAmount;
		};
		if (nextLedger.isNegative() || nextLocked.isNegative()) {
			throw new IllegalArgumentException("Cannot confirm reservation due to invalid balance transition");
		}
		return new BalanceState(
			key,
			nextLedger,
			nextLocked,
			pendingDebitAmount,
			pendingCreditAmount,
			version + 1,
			ledgerAsOfSequence + 1,
			reservationAsOfSequence + 1,
			reconciliationStatus,
			now);
	}

	public BalanceState release(BalanceDirection direction, MoneyAmount amount, Instant now) {
		MoneyAmount nextLocked = switch (direction) {
			case DEBIT -> lockedAmount.subtract(amount);
			case CREDIT -> lockedAmount;
		};
		if (nextLocked.isNegative()) {
			throw new IllegalArgumentException("Cannot release reservation due to invalid locked balance");
		}
		return new BalanceState(
			key,
			ledgerBalance,
			nextLocked,
			pendingDebitAmount,
			pendingCreditAmount,
			version + 1,
			ledgerAsOfSequence,
			reservationAsOfSequence + 1,
			reconciliationStatus,
			now);
	}

	public BalanceStateRecord toRecord() {
		return new BalanceStateRecord(
			key,
			ledgerBalance,
			lockedAmount,
			pendingDebitAmount,
			pendingCreditAmount,
			version,
			ledgerAsOfSequence,
			reservationAsOfSequence,
			reconciliationStatus,
			updatedAt);
	}
}
