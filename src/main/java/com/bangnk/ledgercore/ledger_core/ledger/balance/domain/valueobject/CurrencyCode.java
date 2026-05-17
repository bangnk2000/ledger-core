package com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject;

import java.util.Locale;
import java.util.Objects;

public record CurrencyCode(String value) {

	public CurrencyCode {
		Objects.requireNonNull(value, "currency must not be null");
		value = value.trim().toUpperCase(Locale.ROOT);
		if (!value.matches("[A-Z]{3}")) {
			throw new IllegalArgumentException("currency must be an ISO-style 3-letter code");
		}
	}
}
