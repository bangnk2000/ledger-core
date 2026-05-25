package com.bangnk.ledgercore.ledger_core.balance.adapter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bangnk.ledgercore.ledger_core.balance.application.BalanceApplicationErrors.BalanceDomainException;
import com.bangnk.ledgercore.ledger_core.balance.application.BalanceConsistencyGuard;
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
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BalanceFailClosedIntegrationTest extends PostgresIntegrationTestSupport {

	@Autowired
	private ReserveFundsService reserveFundsService;

	@Autowired
	private BalanceConsistencyGuard consistencyGuard;

	@AfterEach
	void resetGuard() {
		consistencyGuard.setDegraded(false);
	}

	@Test
	void blocksProtectedWritesWhenBalanceManagementIsDegraded() {
		consistencyGuard.setDegraded(true);

		assertThatThrownBy(() -> reserveFundsService.reserve(request("acc-degraded-1", "degraded-req-1", "5.0000")))
			.isInstanceOf(BalanceDomainException.class)
			.hasMessageContaining("blocked while balance-management is degraded");
	}

	private static BalanceMutationRequest request(String accountId, String requestId, String amount) {
		return new BalanceMutationRequest(
			new RequestIdentity("degraded-it", requestId),
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
