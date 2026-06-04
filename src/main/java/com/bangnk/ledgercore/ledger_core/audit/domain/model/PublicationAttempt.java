package com.bangnk.ledgercore.ledger_core.audit.domain.model;

import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.PublicationResult;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class PublicationAttempt {
    private final UUID id;
    private final UUID eventId;
    private final String destinationType;
    private final Instant attemptedAt;
    private final PublicationResult result;
    private final String failureReason;
    private final Instant nextRetryAt;

    @Builder
    public PublicationAttempt(
        UUID id,
        UUID eventId,
        String destinationType,
        Instant attemptedAt,
        PublicationResult result,
        String failureReason,
        Instant nextRetryAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.eventId = Objects.requireNonNull(eventId, "eventId must not be null");
        this.destinationType = normalizeRequired(destinationType, "destinationType", 64);
        this.attemptedAt = Objects.requireNonNull(attemptedAt, "attemptedAt must not be null");
        this.result = Objects.requireNonNull(result, "result must not be null");
        this.failureReason = normalizeOptional(failureReason, "failureReason", 256);
        this.nextRetryAt = nextRetryAt;
    }

    private static String normalizeRequired(String value, String field, int maxLength) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be at most " + maxLength + " characters");
        }
        return value;
    }

    private static String normalizeOptional(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be at most " + maxLength + " characters");
        }
        return value;
    }
}
