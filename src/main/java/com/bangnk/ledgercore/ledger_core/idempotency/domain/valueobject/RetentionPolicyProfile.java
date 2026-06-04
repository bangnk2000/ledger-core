package com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject;

import java.time.Duration;
import java.util.Objects;

public record RetentionPolicyProfile(
    String profileName,
    Duration replayWindowDuration,
    Duration tombstoneDuration
) {
    public RetentionPolicyProfile {
        Objects.requireNonNull(profileName, "profileName must not be null");
        Objects.requireNonNull(replayWindowDuration, "replayWindowDuration must not be null");
        Objects.requireNonNull(tombstoneDuration, "tombstoneDuration must not be null");
        if (profileName.isBlank()) throw new IllegalArgumentException("profileName must not be blank");
        if (replayWindowDuration.isNegative() || replayWindowDuration.isZero()) {
            throw new IllegalArgumentException("replayWindowDuration must be positive");
        }
        if (tombstoneDuration.isNegative()) {
            throw new IllegalArgumentException("tombstoneDuration must not be negative");
        }
    }
    
    // Default policy profile
    public static final RetentionPolicyProfile DEFAULT = new RetentionPolicyProfile(
        "DEFAULT",
        Duration.ofHours(24),
        Duration.ofDays(7)
    );
}
