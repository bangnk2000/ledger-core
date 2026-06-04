package com.bangnk.ledgercore.ledger_core.audit.domain.valueobject;

import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ReferenceStatus;
import java.util.Objects;

public record IdempotencyReference(
    String idempotencyKey,
    String idempotencyRecordId,
    String scopeType,
    String scopeValue,
    ReferenceStatus referenceStatus
) {
    public IdempotencyReference {
        Objects.requireNonNull(referenceStatus, "referenceStatus must not be null");
        idempotencyKey = normalizeOptional(idempotencyKey, "idempotencyKey", 256);
        idempotencyRecordId = normalizeOptional(idempotencyRecordId, "idempotencyRecordId", 128);
        scopeType = normalizeOptional(scopeType, "scopeType", 128);
        scopeValue = normalizeOptional(scopeValue, "scopeValue", 128);
        if (referenceStatus == ReferenceStatus.PRESENT && idempotencyKey == null && idempotencyRecordId == null) {
            throw new IllegalArgumentException("idempotencyKey or idempotencyRecordId must be present when referenceStatus is PRESENT");
        }
    }

    private static String normalizeOptional(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be at most " + maxLength + " characters");
        }
        return value;
    }
}
