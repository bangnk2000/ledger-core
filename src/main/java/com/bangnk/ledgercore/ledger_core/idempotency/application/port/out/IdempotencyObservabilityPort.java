package com.bangnk.ledgercore.ledger_core.idempotency.application.port.out;

public interface IdempotencyObservabilityPort {
    void trackDuplicateSeen(String scopeType, String scopeValue, String key);
    void trackConflictDetected(String scopeType, String scopeValue, String key);
    void trackClaimGranted(String scopeType, String scopeValue, String key);
    void trackDuplicateInProgress(String scopeType, String scopeValue, String key);
    void trackIndeterminateRecorded(String scopeType, String scopeValue, String key);
    void trackExpiration(String scopeType, String scopeValue, String key);
    void trackCleanup(int cleanedCount);
}
