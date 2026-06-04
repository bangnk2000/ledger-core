package com.bangnk.ledgercore.ledger_core.idempotency.domain.model;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.LifecycleEventType;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record LifecycleEvent(
    UUID eventId,
    UUID recordId,
    LifecycleEventType eventType,
    Instant recordedAt,
    String actorType,
    String actorId,
    String details
) {
    public LifecycleEvent {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(recordId, "recordId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(recordedAt, "recordedAt must not be null");
        Objects.requireNonNull(actorType, "actorType must not be null");
        Objects.requireNonNull(actorId, "actorId must not be null");
    }

    public static LifecycleEvent create(
        UUID recordId,
        LifecycleEventType eventType,
        String actorType,
        String actorId,
        String details
    ) {
        return new LifecycleEvent(
            UUID.randomUUID(),
            recordId,
            eventType,
            Instant.now(),
            actorType,
            actorId,
            details
        );
    }
}
