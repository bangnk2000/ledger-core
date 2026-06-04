package com.bangnk.ledgercore.ledger_core.idempotency.domain.model;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.IdempotencyState;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.RetentionStatus;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.ClaimOwner;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyKey;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyScope;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.RequestFingerprint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class IdempotencyRecord {
    private final UUID id;
    private final IdempotencyScope scope;
    private final IdempotencyKey key;
    private final RequestFingerprint fingerprint;
    private IdempotencyState state;
    private ClaimOwner claimOwner;
    private Instant claimAcquiredAt;
    private Instant lastTransitionAt;
    private final Instant firstSeenAt;
    private Instant lastSeenAt;
    private Instant replayWindowExpiresAt;
    private Instant tombstoneExpiresAt;
    private RetentionStatus retentionStatus;
    private final String businessReference;
    private final String correlationId;
    private final String causationId;
    private int attemptCount;
    private ReplayOutcome replayOutcome;
    
    private final List<LifecycleEvent> lifecycleEvents;

    public IdempotencyRecord(
        UUID id,
        IdempotencyScope scope,
        IdempotencyKey key,
        RequestFingerprint fingerprint,
        IdempotencyState state,
        ClaimOwner claimOwner,
        Instant claimAcquiredAt,
        Instant lastTransitionAt,
        Instant firstSeenAt,
        Instant lastSeenAt,
        Instant replayWindowExpiresAt,
        Instant tombstoneExpiresAt,
        RetentionStatus retentionStatus,
        String businessReference,
        String correlationId,
        String causationId,
        int attemptCount,
        ReplayOutcome replayOutcome,
        List<LifecycleEvent> lifecycleEvents
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.scope = scope;
        this.key = key;
        this.fingerprint = fingerprint;
        this.state = state != null ? state : IdempotencyState.RECEIVED;
        this.claimOwner = claimOwner;
        this.claimAcquiredAt = claimAcquiredAt;
        this.lastTransitionAt = lastTransitionAt != null ? lastTransitionAt : Instant.now();
        this.firstSeenAt = firstSeenAt != null ? firstSeenAt : Instant.now();
        this.lastSeenAt = lastSeenAt != null ? lastSeenAt : Instant.now();
        this.replayWindowExpiresAt = replayWindowExpiresAt;
        this.tombstoneExpiresAt = tombstoneExpiresAt;
        this.retentionStatus = retentionStatus != null ? retentionStatus : RetentionStatus.REPLAYABLE;
        this.businessReference = businessReference;
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.attemptCount = attemptCount > 0 ? attemptCount : 1;
        this.replayOutcome = replayOutcome;
        this.lifecycleEvents = lifecycleEvents != null ? new ArrayList<>(lifecycleEvents) : new ArrayList<>();
    }
}
