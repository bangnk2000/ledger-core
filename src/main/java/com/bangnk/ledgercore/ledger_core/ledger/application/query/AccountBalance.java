package com.bangnk.ledgercore.ledger_core.ledger.application.query;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountBalance(
		String accountId,
		BigDecimal balance,
		String currency,
		Instant calculatedAt,
		long entryCount
) {
}
