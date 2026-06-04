package com.bangnk.ledgercore.ledger_core.audit.domain.valueobject;

import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.PresenceStatus;
import java.util.Objects;

public record ActorIdentity(
    ActorType actorType,
    String actorId,
    String origin,
    String authorityContext,
    PresenceStatus presenceStatus
) {
    public ActorIdentity {
        Objects.requireNonNull(actorType, "actorType must not be null");
        Objects.requireNonNull(presenceStatus, "presenceStatus must not be null");
        actorId = normalizeOptional(actorId, "actorId", 128);
        origin = normalizeOptional(origin, "origin", 128);
        authorityContext = normalizeOptional(authorityContext, "authorityContext", 128);
        if (presenceStatus == PresenceStatus.KNOWN && actorId == null) {
            throw new IllegalArgumentException("actorId must be present when presenceStatus is KNOWN");
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
