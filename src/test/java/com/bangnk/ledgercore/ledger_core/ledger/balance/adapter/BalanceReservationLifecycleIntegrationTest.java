package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web.ReservationDtos.ConfirmReservationRequest.FinalizationType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.ReservationLifecycleService;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.ReserveFundsService;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.command.BalanceMutationRequest;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceActorType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.ReservationStatus;
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

class BalanceReservationLifecycleIntegrationTest extends PostgresIntegrationTestSupport {

	@Autowired
	private ReserveFundsService reserveFundsService;
	@Autowired
	private ReservationLifecycleService reservationLifecycleService;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void confirmsReservationAndAdjustsLedgerAndLockExactlyOnce() {
		seedState("acc-lifecycle-1", "10.0000");
		var reserveResult = reserveFundsService.reserve(request("acc-lifecycle-1", "reserve-lifecycle-1", "3.0000", BalanceDirection.DEBIT));
		assertThat(reserveResult.outcome().outcome().name()).isEqualTo("ACCEPTED");

		var confirmResult = reservationLifecycleService.confirm(
			reserveResult.reservationId(),
			new RequestIdentity("integration", "confirm-lifecycle-1"),
			FinalizationType.POST_DEBIT,
			"ledger-txn-1",
			new ActorContext("it-user", BalanceActorType.SYSTEM, "corr-confirm-1", null));
		assertThat(confirmResult.outcome().outcome().name()).isEqualTo("ACCEPTED");

		assertThat(value("select ledger_balance from balance_state where account_id = 'acc-lifecycle-1' and currency = 'USD'"))
			.isEqualByComparingTo("7.0000");
		assertThat(value("select locked_amount from balance_state where account_id = 'acc-lifecycle-1' and currency = 'USD'"))
			.isEqualByComparingTo("0.0000");
		assertThat(jdbcTemplate.queryForObject("select status from funds_reservations where reservation_id = ?", String.class, reserveResult.reservationId()))
			.isEqualTo(ReservationStatus.CONFIRMED.name());
	}

	private void seedState(String accountId, String ledgerBalance) {
		jdbcTemplate.update("""
			INSERT INTO balance_state (
				account_id, currency, ledger_balance, locked_amount, pending_debit_amount, pending_credit_amount,
				version, ledger_as_of_sequence, reservation_as_of_sequence, reconciliation_status, updated_at
			) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
			""", accountId, "USD", new BigDecimal(ledgerBalance), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 1L, 10L, 0L, "HEALTHY",
			Timestamp.from(Instant.parse("2026-05-16T11:00:00Z")));
	}

	private BigDecimal value(String sql) {
		return jdbcTemplate.queryForObject(sql, BigDecimal.class);
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
			Instant.parse("2026-05-30T12:00:00Z"));
	}
}
