package com.bangnk.ledgercore.ledger_core.balance.application.port.out;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceSnapshot;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceSnapshot.ConsistencyMode;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReconciliationStatus;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface BalanceSnapshotRepositoryPort {

	Optional<BalanceSnapshotRecord> findLatest(AccountId accountId, CurrencyCode currency);

	BalanceSnapshotRecord save(BalanceSnapshotRecord snapshot);

	record BalanceSnapshotRecord(
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
		public BalanceSnapshot toDomain() {
			return new BalanceSnapshot(
				snapshotId,
				accountId,
				currency,
				ledgerBalance,
				lockedAmount,
				pendingDebitAmount,
				pendingCreditAmount,
				availableBalance,
				snapshotVersion,
				asOfSequence,
				asOfTime,
				consistencyMode,
				reconciliationStatus,
				createdAt);
		}

		public static BalanceSnapshotRecord fromDomain(BalanceSnapshot snapshot) {
			return new BalanceSnapshotRecord(
				snapshot.snapshotId(),
				snapshot.accountId(),
				snapshot.currency(),
				snapshot.ledgerBalance(),
				snapshot.lockedAmount(),
				snapshot.pendingDebitAmount(),
				snapshot.pendingCreditAmount(),
				snapshot.availableBalance(),
				snapshot.snapshotVersion(),
				snapshot.asOfSequence(),
				snapshot.asOfTime(),
				snapshot.consistencyMode(),
				snapshot.reconciliationStatus(),
				snapshot.createdAt());
		}
	}
}
