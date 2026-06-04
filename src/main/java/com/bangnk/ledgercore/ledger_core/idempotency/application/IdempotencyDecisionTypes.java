package com.bangnk.ledgercore.ledger_core.idempotency.application;

public enum IdempotencyDecisionTypes {
    FIRST_EXECUTION,
    REPLAY,
    DUPLICATE_IN_PROGRESS,
    CONFLICT,
    EXPIRED_KEY,
    INDETERMINATE
}
