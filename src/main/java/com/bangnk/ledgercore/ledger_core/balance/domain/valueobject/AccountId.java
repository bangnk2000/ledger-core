package com.bangnk.ledgercore.ledger_core.balance.domain.valueobject;

import java.util.Objects;

public record AccountId(String value) {

	public AccountId {
		value = normalize(value, "accountId");
	}

	private static String normalize(String value, String field) {
		Objects.requireNonNull(value, field + " must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException(field + " must not be blank");
		}
		if (value.length() > 128) {
			throw new IllegalArgumentException(field + " must be at most 128 characters");
		}
		return value;
	}
}
