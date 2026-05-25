package com.bangnk.ledgercore.ledger_core.balance.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record MoneyAmount(BigDecimal value) implements Comparable<MoneyAmount> {

	private static final int SCALE = 4;

	public MoneyAmount {
		Objects.requireNonNull(value, "amount must not be null");
		value = value.setScale(SCALE, RoundingMode.UNNECESSARY);
	}

	public static MoneyAmount zero() {
		return new MoneyAmount(BigDecimal.ZERO);
	}

	public static MoneyAmount positive(BigDecimal value) {
		MoneyAmount amount = new MoneyAmount(value);
		if (!amount.isPositive()) {
			throw new IllegalArgumentException("amount must be positive");
		}
		return amount;
	}

	public MoneyAmount add(MoneyAmount other) {
		return new MoneyAmount(value.add(other.value));
	}

	public MoneyAmount subtract(MoneyAmount other) {
		return new MoneyAmount(value.subtract(other.value));
	}

	public boolean isPositive() {
		return value.signum() > 0;
	}

	public boolean isNegative() {
		return value.signum() < 0;
	}

	public boolean isZero() {
		return value.signum() == 0;
	}

	@Override
	public int compareTo(MoneyAmount other) {
		return value.compareTo(other.value);
	}
}
