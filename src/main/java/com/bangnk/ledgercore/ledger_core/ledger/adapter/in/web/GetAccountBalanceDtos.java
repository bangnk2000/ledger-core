package com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.ledger.application.query.AccountBalance;
import java.math.RoundingMode;
import java.time.Instant;

public final class GetAccountBalanceDtos {

	private GetAccountBalanceDtos() {
	}

	public record AccountBalanceResponse(
			String accountId,
			String balance,
			String currency,
			Instant calculatedAt,
			long entryCount
	) {
		public static AccountBalanceResponse from(AccountBalance balance) {
			return new AccountBalanceResponse(
				balance.accountId(),
				balance.balance().setScale(4, RoundingMode.UNNECESSARY).toPlainString(),
				balance.currency(),
				balance.calculatedAt(),
				balance.entryCount());
		}
	}
}
