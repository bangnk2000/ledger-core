package com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.RetentionStatus;
import java.time.Duration;
import java.time.Instant;
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

    public Instant replayWindowExpiresAt(Instant firstSeenAt) {
        return firstSeenAt.plus(replayWindowDuration);
    }

    public Instant tombstoneExpiresAt(Instant firstSeenAt) {
        return replayWindowExpiresAt(firstSeenAt).plus(tombstoneDuration);
    }

    public RetentionStatus retentionStatusAt(
        Instant now,
        Instant replayWindowExpiresAt,
        Instant tombstoneExpiresAt
    ) {
        if (now.isAfter(tombstoneExpiresAt)) {
            return RetentionStatus.PURGE_ELIGIBLE;
        }
        if (now.isAfter(replayWindowExpiresAt)) {
            return RetentionStatus.TOMBSTONED;
        }
        return RetentionStatus.REPLAYABLE;
    }
}
