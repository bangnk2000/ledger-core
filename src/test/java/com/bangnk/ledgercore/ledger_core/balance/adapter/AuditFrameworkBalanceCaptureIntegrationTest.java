package com.bangnk.ledgercore.ledger_core.balance.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.balance.adapter.in.web.ReservationDtos.ConfirmReservationRequest.FinalizationType;
import com.bangnk.ledgercore.ledger_core.balance.adapter.in.web.ReservationDtos.ReleaseReservationRequest.ReleaseReason;
import com.bangnk.ledgercore.ledger_core.balance.application.ReservationLifecycleService;
import com.bangnk.ledgercore.ledger_core.balance.application.ReserveFundsService;
import com.bangnk.ledgercore.ledger_core.balance.application.command.BalanceMutationRequest;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceActorType;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class AuditFrameworkBalanceCaptureIntegrationTest extends PostgresIntegrationTestSupport {

	@Autowired
	private ReserveFundsService reserveFundsService;

	@Autowired
	private ReservationLifecycleService reservationLifecycleService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void storesSharedAuditEvidenceForReserveConfirmAndReleaseFlows() {
		seedState("acc-audit-balance", "10.0000");

		var reserveResult = reserveFundsService.reserve(request("reserve-audit-1", BalanceDirection.DEBIT));
		assertThat(reserveResult.outcome().outcome().name()).isEqualTo("ACCEPTED");

		var confirmResult = reservationLifecycleService.confirm(
			reserveResult.reservationId(),
			new RequestIdentity("integration", "confirm-audit-1"),
			FinalizationType.POST_DEBIT,
			"ledger-txn-confirm-1",
			new ActorContext("svc-balance", BalanceActorType.SERVICE, "corr-confirm-audit-1", null));
		assertThat(confirmResult.outcome().outcome().name()).isEqualTo("ACCEPTED");

		var releaseResult = reservationLifecycleService.release(
			reserveResult.reservationId(),
			new RequestIdentity("integration", "release-audit-1"),
			ReleaseReason.FAILED_WORKFLOW,
			new ActorContext("svc-balance", BalanceActorType.SERVICE, "corr-release-audit-1", null));
		assertThat(releaseResult.outcome().outcome().name()).isEqualTo("DUPLICATE");

		assertThat(jdbcTemplate.queryForObject(
			"select count(*) from audit_events where module_name = 'balance'",
			Integer.class)).isEqualTo(3);
		assertThat(jdbcTemplate.queryForObject(
			"select count(*) from audit_publication_attempts",
			Integer.class)).isEqualTo(3);
	}

	private void seedState(String accountId, String ledgerBalance) {
		jdbcTemplate.update("""
			INSERT INTO balance_state (
				account_id, currency, ledger_balance, locked_amount, pending_debit_amount, pending_credit_amount,
				version, ledger_as_of_sequence, reservation_as_of_sequence, reconciliation_status, updated_at
			) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
			""", accountId, "USD", new BigDecimal(ledgerBalance), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 1L, 10L, 0L, "HEALTHY",
			Timestamp.from(Instant.parse("2026-06-04T11:00:00Z")));
	}

	private static BalanceMutationRequest request(String requestId, BalanceDirection direction) {
		return new BalanceMutationRequest(
			new RequestIdentity("integration", requestId),
			BalanceMutationType.RESERVE,
			List.of(new AccountId("acc-audit-balance")),
			new CurrencyCode("USD"),
			new MoneyAmount(new BigDecimal("3.0000")),
			direction,
			new ActorContext("svc-balance", BalanceActorType.SERVICE, "corr-" + requestId, null),
			"reservation-" + requestId,
			Instant.parse("2026-06-05T00:00:00Z"));
	}
}
