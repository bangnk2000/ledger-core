package com.bangnk.ledgercore.ledger_core.idempotency.domain.model;

public final class IdempotencyEnums {
    private IdempotencyEnums() {}

    public enum IdempotencyState {
        RECEIVED,
        CLAIMED,
        PROCESSING,
        COMPLETED,
        REJECTED,
        INDETERMINATE
    }

    public enum RetentionStatus {
        REPLAYABLE,
        TOMBSTONED,
        PURGE_ELIGIBLE
    }

    public enum OutcomeType {
        FIRST_EXECUTION,
        REPLAY,
        DUPLICATE_IN_PROGRESS,
        CONFLICT,
        EXPIRED_KEY,
        INDETERMINATE
    }

    public enum LifecycleEventType {
        CLAIM_GRANTED,
        DUPLICATE_SEEN,
        CONFLICT_DETECTED,
        PROCESSING_STARTED,
        COMPLETED,
        REPLAY_WINDOW_ELAPSED,
        TOMBSTONE_ELAPSED,
        ARCHIVED,
        INDETERMINATE_RECORDED
    }
}
