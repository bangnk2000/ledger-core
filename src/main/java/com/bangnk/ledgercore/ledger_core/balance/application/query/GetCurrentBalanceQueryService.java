package com.bangnk.ledgercore.ledger_core.balance.application.query;

import com.bangnk.ledgercore.ledger_core.balance.application.BalanceObservability;
import com.bangnk.ledgercore.ledger_core.balance.application.port.in.GetCurrentBalanceUseCase;
import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceSnapshotRepositoryPort;
import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceStateRepositoryPort;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceSnapshot;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceSnapshot.ConsistencyMode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class GetCurrentBalanceQueryService implements GetCurrentBalanceUseCase {

	private final BalanceStateRepositoryPort balanceStateRepository;
	private final BalanceSnapshotRepositoryPort snapshotRepository;
	private final BalanceObservability observability;
	private final Clock clock;

	public GetCurrentBalanceQueryService(
			BalanceStateRepositoryPort balanceStateRepository,
			BalanceSnapshotRepositoryPort snapshotRepository,
			BalanceObservability observability,
			Clock clock
	) {
		this.balanceStateRepository = balanceStateRepository;
		this.snapshotRepository = snapshotRepository;
		this.observability = observability;
		this.clock = clock;
	}

	@Override
	public BalanceSnapshot getCurrentBalance(AccountId accountId, CurrencyCode currency, ConsistencyMode consistencyMode) {
		Instant now = Instant.now(clock);
		BalanceSnapshot snapshot = balanceStateRepository.findByAccountAndCurrency(accountId, currency)
			.map(state -> BalanceSnapshot.fromState(state, consistencyMode, now))
			.orElseGet(() -> BalanceSnapshot.empty(accountId, currency, consistencyMode, now));
		snapshotRepository.save(BalanceSnapshotRepositoryPort.BalanceSnapshotRecord.fromDomain(snapshot));
		observability.recordBalanceRead(consistencyMode);
		return snapshot;
	}
}
