package com.bangnk.ledgercore.ledger_core.audit.application;

import java.time.Instant;

public record AuditQueryCriteria(
    String subjectType,
    String subjectId,
    String businessReference,
    String actorType,
    String actorId,
    String eventType,
    String moduleName,
    Instant fromOccurredAt,
    Instant toOccurredAt,
    String correlationId,
    String requestId,
    String ledgerTransactionId,
    String idempotencyKey,
    String idempotencyRecordId
) {
}
