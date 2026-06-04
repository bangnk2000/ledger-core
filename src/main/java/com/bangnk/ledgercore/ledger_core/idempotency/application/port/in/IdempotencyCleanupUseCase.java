package com.bangnk.ledgercore.ledger_core.idempotency.application.port.in;

import java.time.Instant;

public interface IdempotencyCleanupUseCase {
    int cleanupExpiredRecords(Instant now);
}
