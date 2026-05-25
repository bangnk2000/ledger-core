package com.bangnk.ledgercore.ledger_core.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceSnapshotRepositoryPort;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaBalanceSnapshotRepositoryAdapter implements BalanceSnapshotRepositoryPort {

	private final BalanceSnapshotJpaRepository repository;

	public JpaBalanceSnapshotRepositoryAdapter(BalanceSnapshotJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<BalanceSnapshotRecord> findLatest(AccountId accountId, CurrencyCode currency) {
		return repository.findTopByAccountIdAndCurrencyOrderBySnapshotVersionDesc(accountId.value(), currency.value()).map(this::toRecord);
	}

	@Override
	public BalanceSnapshotRecord save(BalanceSnapshotRecord snapshot) {
		repository.save(toEntity(snapshot));
		return snapshot;
	}

	private BalanceSnapshotRecord toRecord(BalanceSnapshotJpaEntity entity) {
		return new BalanceSnapshotRecord(
			entity.getSnapshotId(),
			new AccountId(entity.getAccountId()),
			new CurrencyCode(entity.getCurrency()),
			new MoneyAmount(entity.getLedgerBalance()),
			new MoneyAmount(entity.getLockedAmount()),
			new MoneyAmount(entity.getPendingDebitAmount()),
			new MoneyAmount(entity.getPendingCreditAmount()),
			new MoneyAmount(entity.getAvailableBalance()),
			entity.getSnapshotVersion(),
			entity.getAsOfSequence(),
			entity.getAsOfTime(),
			entity.getConsistencyMode(),
			entity.getReconciliationStatus(),
			entity.getCreatedAt());
	}

	private BalanceSnapshotJpaEntity toEntity(BalanceSnapshotRecord record) {
		return new BalanceSnapshotJpaEntity(
			record.snapshotId(),
			record.accountId().value(),
			record.currency().value(),
			record.ledgerBalance().value(),
			record.lockedAmount().value(),
			record.pendingDebitAmount().value(),
			record.pendingCreditAmount().value(),
			record.availableBalance().value(),
			record.snapshotVersion(),
			record.asOfSequence(),
			record.asOfTime(),
			record.consistencyMode(),
			record.reconciliationStatus(),
			record.createdAt());
	}
}
