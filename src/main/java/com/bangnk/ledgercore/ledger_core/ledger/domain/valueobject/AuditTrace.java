package com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject;

import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record AuditTrace(
		RequestIdentity requestIdentity,
		String correlationId,
		String causationId,
		Actor actor,
		Instant submittedAt
) {
	public AuditTrace {
		Objects.requireNonNull(requestIdentity, "requestIdentity must not be null");
		Objects.requireNonNull(actor, "actor must not be null");
	}

	public record Actor(String actorId, ActorType actorType) {
		public Actor {
			Objects.requireNonNull(actorId, "actorId must not be null");
			Objects.requireNonNull(actorType, "actorType must not be null");
			if (actorId.isBlank()) {
				throw new IllegalArgumentException("actorId must not be blank");
			}
		}
	}

	public record AuditEvent(
			String eventType,
			RequestIdentity requestIdentity,
			String transactionId,
			Actor actor,
			String correlationId,
			String causationId,
			Instant eventTime,
			Map<String, Object> safeDetails
	) {
		public AuditEvent {
			Objects.requireNonNull(eventType, "eventType must not be null");
			Objects.requireNonNull(requestIdentity, "requestIdentity must not be null");
			Objects.requireNonNull(actor, "actor must not be null");
			Objects.requireNonNull(eventTime, "eventTime must not be null");
			safeDetails = safeDetails == null ? Map.of() : Map.copyOf(safeDetails);
		}
	}
}
