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

class BalanceIdempotencyIntegrationTest extends PostgresIntegrationTestSupport {

	@Autowired
	private ReserveFundsService reserveFundsService;

	@Test
	void returnsDuplicateForSameRequestAndConflictForDifferentIntentWithSameIdentity() {
		BalanceMutationRequest initial = request("acc-idem-1", "idem-req-1", "10.0000");
		assertThat(reserveFundsService.reserve(initial).outcome().outcome().name()).isEqualTo("ACCEPTED");

		BalanceMutationRequest duplicate = request("acc-idem-1", "idem-req-1", "10.0000");
		assertThat(reserveFundsService.reserve(duplicate).outcome().outcome().name()).isEqualTo("DUPLICATE");

		BalanceMutationRequest conflict = request("acc-idem-1", "idem-req-1", "11.0000");
		assertThat(reserveFundsService.reserve(conflict).outcome().outcome().name()).isEqualTo("CONFLICT");
	}

	private static BalanceMutationRequest request(String accountId, String requestId, String amount) {
		return new BalanceMutationRequest(
			new RequestIdentity("idempotency-it", requestId),
			BalanceMutationType.RESERVE,
			List.of(new AccountId(accountId)),
			new CurrencyCode("USD"),
			new MoneyAmount(new BigDecimal(amount)),
			BalanceDirection.CREDIT,
			new ActorContext("it-user", BalanceActorType.SYSTEM, "corr-" + requestId, null),
			"order-" + requestId,
			Instant.parse("2026-05-20T12:00:00Z"));
	}
}
