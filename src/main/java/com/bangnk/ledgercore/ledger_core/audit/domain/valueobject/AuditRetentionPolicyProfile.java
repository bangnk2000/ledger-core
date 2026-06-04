package com.bangnk.ledgercore.ledger_core.audit.domain.valueobject;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record AuditRetentionPolicyProfile(
    String profileName,
    Duration activeRetention,
    Duration restrictedRetention,
    String finalDispositionRule,
    String regulatoryClassification
) {
    public AuditRetentionPolicyProfile {
        profileName = normalizeRequired(profileName, "profileName", 128);
        Objects.requireNonNull(activeRetention, "activeRetention must not be null");
        Objects.requireNonNull(restrictedRetention, "restrictedRetention must not be null");
        if (activeRetention.isNegative() || activeRetention.isZero()) {
            throw new IllegalArgumentException("activeRetention must be positive");
        }
        if (restrictedRetention.isNegative() || restrictedRetention.isZero()) {
            throw new IllegalArgumentException("restrictedRetention must be positive");
        }
        finalDispositionRule = normalizeRequired(finalDispositionRule, "finalDispositionRule", 64);
        regulatoryClassification = normalizeRequired(regulatoryClassification, "regulatoryClassification", 64);
    }

    public Instant activeRetentionUntil(Instant capturedAt) {
        Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        return capturedAt.plus(activeRetention);
    }

    public Instant restrictedRetentionUntil(Instant capturedAt) {
        Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        return capturedAt.plus(activeRetention).plus(restrictedRetention);
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
}
