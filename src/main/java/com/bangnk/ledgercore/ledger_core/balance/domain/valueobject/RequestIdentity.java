package com.bangnk.ledgercore.ledger_core.balance.domain.valueobject;

import java.util.Objects;

public record RequestIdentity(String requesterScope, String requestId) {

	public RequestIdentity {
		requesterScope = normalize(requesterScope, "requesterScope");
		requestId = normalize(requestId, "requestId");
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

	public record RequestHash(String value) {
		public RequestHash {
			value = normalize(value, "requestHash");
		}
	}
}
