package com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject;

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
		return value;
	}

	public record RequestHash(String value) {
		public RequestHash {
			Objects.requireNonNull(value, "requestHash must not be null");
			if (value.isBlank()) {
				throw new IllegalArgumentException("requestHash must not be blank");
			}
		}
	}
}
