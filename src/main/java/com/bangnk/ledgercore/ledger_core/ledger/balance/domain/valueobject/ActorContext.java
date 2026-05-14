package com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject;

import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceActorType;
import java.util.Objects;
import java.util.Optional;

public record ActorContext(
		String actorId,
		BalanceActorType actorType,
		String correlationId,
		String causationId
) {

	public ActorContext {
		actorId = normalizeRequired(actorId, "actorId");
		Objects.requireNonNull(actorType, "actorType must not be null");
		correlationId = normalizeOptional(correlationId, "correlationId");
		causationId = normalizeOptional(causationId, "causationId");
	}

	public Optional<String> optionalCorrelationId() {
		return Optional.ofNullable(correlationId);
	}

	public Optional<String> optionalCausationId() {
		return Optional.ofNullable(causationId);
	}

	private static String normalizeRequired(String value, String field) {
		Objects.requireNonNull(value, field + " must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException(field + " must not be blank");
		}
		if (value.length() > 128) {
			throw new IllegalArgumentException(field + " must be at most 128 characters");
		}
		return value;
	}

	private static String normalizeOptional(String value, String field) {
		if (value == null || value.isBlank()) {
			return null;
		}
		if (value.length() > 128) {
			throw new IllegalArgumentException(field + " must be at most 128 characters");
		}
		return value;
	}
}
