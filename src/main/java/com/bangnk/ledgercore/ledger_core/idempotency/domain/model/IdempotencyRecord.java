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

    @Builder
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

    public void claim(ClaimOwner claimOwner, Instant claimAcquiredAt) {
        if (this.state != IdempotencyState.RECEIVED) {
            throw new IllegalStateException("Cannot claim a record in state: " + this.state);
        }
        this.state = IdempotencyState.CLAIMED;
        this.claimOwner = claimOwner;
        this.claimAcquiredAt = claimAcquiredAt;
        this.lastTransitionAt = claimAcquiredAt;
    }

    public void startProcessing() {
        if (this.state != IdempotencyState.CLAIMED) {
            throw new IllegalStateException("Cannot start processing from state: " + this.state);
        }
        this.state = IdempotencyState.PROCESSING;
        this.lastTransitionAt = Instant.now();
    }

    public void finalizeRecord(ClaimOwner owner, ReplayOutcome outcome) {
        if (this.claimOwner == null || !this.claimOwner.equals(owner)) {
            throw new IllegalArgumentException("Claim owner mismatch. Expected: " + this.claimOwner + ", Actual: " + owner);
        }
        if (this.state != IdempotencyState.CLAIMED && this.state != IdempotencyState.PROCESSING) {
            throw new IllegalStateException("Cannot finalize a record in state: " + this.state);
        }
        
        this.state = switch (outcome.outcomeType()) {
            case REPLAY, FIRST_EXECUTION -> IdempotencyState.COMPLETED;
            case INDETERMINATE -> IdempotencyState.INDETERMINATE;
            case CONFLICT, DUPLICATE_IN_PROGRESS, EXPIRED_KEY -> IdempotencyState.REJECTED;
        };
        
        this.replayOutcome = outcome;
        this.lastTransitionAt = Instant.now();
    }

    public void refreshRetentionStatus(Instant now) {
        if (replayWindowExpiresAt == null || tombstoneExpiresAt == null) {
            return;
        }
        if (now.isAfter(tombstoneExpiresAt)) {
            retentionStatus = RetentionStatus.PURGE_ELIGIBLE;
            return;
        }
        if (now.isAfter(replayWindowExpiresAt)) {
            retentionStatus = RetentionStatus.TOMBSTONED;
        } else {
            retentionStatus = RetentionStatus.REPLAYABLE;
        }
    }
}
