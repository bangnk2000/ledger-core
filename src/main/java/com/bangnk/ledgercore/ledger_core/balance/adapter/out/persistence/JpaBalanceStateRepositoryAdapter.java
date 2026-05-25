package com.bangnk.ledgercore.ledger_core.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceStateRepositoryPort;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaBalanceStateRepositoryAdapter implements BalanceStateRepositoryPort {

	private final BalanceStateJpaRepository repository;

	public JpaBalanceStateRepositoryAdapter(BalanceStateJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<BalanceStateRecord> findByAccountAndCurrency(AccountId accountId, CurrencyCode currency) {
		return repository.findByAccountIdAndCurrency(accountId.value(), currency.value()).map(this::toRecord);
	}

	@Override
	public Optional<BalanceStateRecord> findByAccountAndCurrencyForUpdate(AccountId accountId, CurrencyCode currency) {
		return repository.findByAccountIdAndCurrencyForUpdate(accountId.value(), currency.value()).map(this::toRecord);
	}

	@Override
	public List<BalanceStateRecord> lockInDeterministicOrder(Collection<BalanceStateKey> keys) {
		List<String> encodedKeys = keys.stream().sorted().map(k -> k.accountId().value() + "|" + k.currency().value()).toList();
		return repository.lockDeterministic(encodedKeys).stream().map(this::toRecord).toList();
	}

	@Override
	public BalanceStateRecord save(BalanceStateRecord state) {
		repository.save(toEntity(state));
		return state;
	}

	private BalanceStateRecord toRecord(BalanceStateJpaEntity entity) {
		return new BalanceStateRecord(
			new BalanceStateKey(new AccountId(entity.getAccountId()), new CurrencyCode(entity.getCurrency())),
			new MoneyAmount(entity.getLedgerBalance()),
			new MoneyAmount(entity.getLockedAmount()),
			new MoneyAmount(entity.getPendingDebitAmount()),
			new MoneyAmount(entity.getPendingCreditAmount()),
			entity.getVersion(),
			entity.getLedgerAsOfSequence(),
			entity.getReservationAsOfSequence(),
			entity.getReconciliationStatus(),
			entity.getUpdatedAt());
	}

	private BalanceStateJpaEntity toEntity(BalanceStateRecord record) {
		return new BalanceStateJpaEntity(
			record.key().accountId().value(),
			record.key().currency().value(),
			record.ledgerBalance().value(),
			record.lockedAmount().value(),
			record.pendingDebitAmount().value(),
			record.pendingCreditAmount().value(),
			record.version(),
			record.ledgerAsOfSequence(),
			record.reservationAsOfSequence(),
			record.reconciliationStatus(),
			record.updatedAt());
	}
}
