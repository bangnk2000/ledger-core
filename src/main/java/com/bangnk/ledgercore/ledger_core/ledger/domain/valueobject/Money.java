package com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) {

	public Money {
		Objects.requireNonNull(amount, "amount must not be null");
		Objects.requireNonNull(currency, "currency must not be null");
		if (currency.isBlank() || currency.length() != 3) {
			throw new IllegalArgumentException("currency must be a 3-letter code");
		}
		amount = amount.setScale(4, RoundingMode.UNNECESSARY);
		if (amount.signum() <= 0) {
			throw new IllegalArgumentException("amount must be positive");
		}
		currency = currency.toUpperCase();
	}

	public Money add(Money other) {
		ensureSameCurrency(other);
		return new Money(amount.add(other.amount), currency);
	}

	public Money negate() {
		return new Money(amount.negate().abs(), currency);
	}

	public void ensureSameCurrency(Money other) {
		if (!currency.equals(other.currency)) {
			throw new IllegalArgumentException("currency mismatch");
		}
	}
}
