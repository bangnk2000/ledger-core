package com.bangnk.ledgercore.ledger_core.audit.domain.valueobject;

import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.PresenceStatus;
import java.util.Objects;

public record TraceContext(
    String correlationId,
    String requestId,
    String causationId,
    PresenceStatus presenceStatus
) {
    public TraceContext {
        Objects.requireNonNull(presenceStatus, "presenceStatus must not be null");
        correlationId = normalizeOptional(correlationId, "correlationId");
        requestId = normalizeOptional(requestId, "requestId");
        causationId = normalizeOptional(causationId, "causationId");
        if (presenceStatus == PresenceStatus.KNOWN && correlationId == null && requestId == null) {
            throw new IllegalArgumentException("correlationId or requestId must be present when presenceStatus is KNOWN");
        }
    }

    private static String normalizeOptional(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() > 128) {
            throw new IllegalArgumentException(field + " must be at most 128 characters");
        }
        return value;
    }
}
