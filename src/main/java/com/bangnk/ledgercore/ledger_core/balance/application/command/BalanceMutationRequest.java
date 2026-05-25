package com.bangnk.ledgercore.ledger_core.balance.application.command;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import java.time.Instant;
import java.util.List;

public record BalanceMutationRequest(
		RequestIdentity requestIdentity,
		BalanceMutationType mutationType,
		List<AccountId> accountIds,
		CurrencyCode currency,
		MoneyAmount amount,
		BalanceDirection direction,
		ActorContext actorContext,
		String businessReference,
		Instant expiresAt
) {
}
