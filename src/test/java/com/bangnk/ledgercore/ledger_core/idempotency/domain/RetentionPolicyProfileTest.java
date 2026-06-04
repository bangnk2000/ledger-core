package com.bangnk.ledgercore.ledger_core.idempotency.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.RetentionStatus;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.RetentionPolicyProfile;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RetentionPolicyProfileTest {

    @Test
    void transitionsFromReplayableToTombstonedToPurgeEligible() {
        RetentionPolicyProfile profile = new RetentionPolicyProfile("FINANCIAL", Duration.ofHours(24), Duration.ofDays(7));
        Instant firstSeen = Instant.parse("2026-06-01T00:00:00Z");
        Instant replayWindowExpiresAt = profile.replayWindowExpiresAt(firstSeen);
        Instant tombstoneExpiresAt = profile.tombstoneExpiresAt(firstSeen);

        assertEquals(RetentionStatus.REPLAYABLE, profile.retentionStatusAt(firstSeen.plus(Duration.ofHours(1)), replayWindowExpiresAt, tombstoneExpiresAt));
        assertEquals(RetentionStatus.TOMBSTONED, profile.retentionStatusAt(replayWindowExpiresAt.plus(Duration.ofMinutes(1)), replayWindowExpiresAt, tombstoneExpiresAt));
        assertEquals(RetentionStatus.PURGE_ELIGIBLE, profile.retentionStatusAt(tombstoneExpiresAt.plus(Duration.ofMinutes(1)), replayWindowExpiresAt, tombstoneExpiresAt));
    }
}
