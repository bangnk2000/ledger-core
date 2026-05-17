package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.ReserveFundsService;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.command.BalanceMutationRequest;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.query.GetCurrentBalanceQueryService;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceActorType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceSnapshot.ConsistencyMode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.RequestIdentity;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class BalanceSnapshotIntegrationTest extends PostgresIntegrationTestSupport {

	@Autowired
	private GetCurrentBalanceQueryService queryService;
	@Autowired
	private ReserveFundsService reserveFundsService;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void returnsZeroSnapshotWhenNoStateExists() {
		var snapshot = queryService.getCurrentBalance(new AccountId("acc-empty"), new CurrencyCode("USD"), ConsistencyMode.STRONG);
		assertThat(snapshot.availableBalance().value()).isEqualByComparingTo("0.0000");
		assertThat(snapshot.snapshotVersion()).isZero();
	}

	@Test
	void returnsPopulatedSnapshotAfterReservations() {
		jdbcTemplate.update("""
			INSERT INTO balance_state (
				account_id, currency, ledger_balance, locked_amount, pending_debit_amount, pending_credit_amount,
				version, ledger_as_of_sequence, reservation_as_of_sequence, reconciliation_status, updated_at
			) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
			""", "acc-populated", "USD", new BigDecimal("10.0000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 1L, 7L, 0L, "HEALTHY",
			Timestamp.from(Instant.parse("2026-05-16T11:00:00Z")));
		reserveFundsService.reserve(request("acc-populated", "reserve-debit-1", "2.0000", BalanceDirection.DEBIT));

		var snapshot = queryService.getCurrentBalance(new AccountId("acc-populated"), new CurrencyCode("USD"), ConsistencyMode.DERIVED);
		assertThat(snapshot.lockedAmount().value()).isEqualByComparingTo("2.0000");
		assertThat(snapshot.availableBalance().value()).isEqualByComparingTo("8.0000");
		assertThat(snapshot.consistencyMode()).isEqualTo(ConsistencyMode.DERIVED);
	}

	private static BalanceMutationRequest request(String accountId, String requestId, String amount, BalanceDirection direction) {
		return new BalanceMutationRequest(
			new RequestIdentity("integration", requestId),
			BalanceMutationType.RESERVE,
			List.of(new AccountId(accountId)),
			new CurrencyCode("USD"),
			new MoneyAmount(new BigDecimal(amount)),
			direction,
			new ActorContext("it-user", BalanceActorType.SYSTEM, "corr-" + requestId, null),
			"order-" + requestId,
			Instant.parse("2026-05-20T12:00:00Z"));
	}
}
