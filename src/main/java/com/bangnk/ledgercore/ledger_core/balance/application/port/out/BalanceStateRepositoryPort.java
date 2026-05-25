package com.bangnk.ledgercore.ledger_core.balance.application.port.out;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReconciliationStatus;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BalanceStateRepositoryPort {

	Optional<BalanceStateRecord> findByAccountAndCurrency(AccountId accountId, CurrencyCode currency);

	Optional<BalanceStateRecord> findByAccountAndCurrencyForUpdate(AccountId accountId, CurrencyCode currency);

	List<BalanceStateRecord> lockInDeterministicOrder(Collection<BalanceStateKey> keys);

	BalanceStateRecord save(BalanceStateRecord state);

	record BalanceStateKey(AccountId accountId, CurrencyCode currency) implements Comparable<BalanceStateKey> {
		@Override
		public int compareTo(BalanceStateKey other) {
			int accountCompare = accountId.value().compareTo(other.accountId.value());
			if (accountCompare != 0) {
				return accountCompare;
			}
			return currency.value().compareTo(other.currency.value());
		}
	}

	record BalanceStateRecord(
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
		public MoneyAmount availableBalance() {
			return ledgerBalance.subtract(lockedAmount).subtract(pendingDebitAmount).add(pendingCreditAmount);
		}
	}
}
