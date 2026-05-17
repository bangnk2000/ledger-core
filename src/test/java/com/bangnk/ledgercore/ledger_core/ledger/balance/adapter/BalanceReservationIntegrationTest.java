package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.ReserveFundsService;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.command.BalanceMutationRequest;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceActorType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.RequestIdentity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BalanceReservationIntegrationTest extends PostgresIntegrationTestSupport {

	@Autowired
	private ReserveFundsService reserveFundsService;

	@Test
	void reservesAndRejectsInsufficientFunds() {
		BalanceMutationRequest accepted = request("acc-int-1", "reserve-int-1", "1.0000", BalanceDirection.CREDIT);
		assertThat(reserveFundsService.reserve(accepted).outcome().outcome().name()).isEqualTo("ACCEPTED");

		BalanceMutationRequest rejected = request("acc-int-2", "reserve-int-2", "1.0000", BalanceDirection.DEBIT);
		assertThat(reserveFundsService.reserve(rejected).outcome().outcome().name()).isEqualTo("REJECTED");
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
