package com.bangnk.ledgercore.ledger_core.audit.domain.model;

import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.AuditEventType;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.ActorIdentity;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.AuditRetentionPolicyProfile;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IdempotencyReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IntegrityProof;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.LedgerTransactionReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.TraceContext;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class AuditEvent {
    private final UUID id;
    private final AuditEventType eventType;
    private final String schemaVersion;
    private final String moduleName;
    private final Instant occurredAt;
    private final Instant capturedAt;
    private final String subjectType;
    private final String subjectId;
    private final String businessReference;
    private final String stateFrom;
    private final String stateTo;
    private final Map<String, Object> safeDetails;
    private final ActorIdentity actorIdentity;
    private final TraceContext traceContext;
    private final LedgerTransactionReference ledgerTransactionReference;
    private final IdempotencyReference idempotencyReference;
    private final AuditRetentionPolicyProfile retentionPolicyProfile;
    private final IntegrityProof integrityProof;

    @Builder
    public AuditEvent(
        UUID id,
        AuditEventType eventType,
        String schemaVersion,
        String moduleName,
        Instant occurredAt,
        Instant capturedAt,
        String subjectType,
        String subjectId,
        String businessReference,
        String stateFrom,
        String stateTo,
        Map<String, Object> safeDetails,
        ActorIdentity actorIdentity,
        TraceContext traceContext,
        LedgerTransactionReference ledgerTransactionReference,
        IdempotencyReference idempotencyReference,
        AuditRetentionPolicyProfile retentionPolicyProfile,
        IntegrityProof integrityProof
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        this.schemaVersion = normalizeRequired(schemaVersion, "schemaVersion", 32);
        this.moduleName = normalizeRequired(moduleName, "moduleName", 128);
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        this.capturedAt = capturedAt != null ? capturedAt : occurredAt;
        this.subjectType = normalizeRequired(subjectType, "subjectType", 128);
        this.subjectId = normalizeRequired(subjectId, "subjectId", 128);
        this.businessReference = normalizeOptional(businessReference, "businessReference", 128);
        this.stateFrom = normalizeOptional(stateFrom, "stateFrom", 128);
        this.stateTo = normalizeOptional(stateTo, "stateTo", 128);
        this.safeDetails = safeDetails == null ? Map.of() : Map.copyOf(safeDetails);
        this.actorIdentity = Objects.requireNonNull(actorIdentity, "actorIdentity must not be null");
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext must not be null");
        this.ledgerTransactionReference = Objects.requireNonNull(ledgerTransactionReference, "ledgerTransactionReference must not be null");
        this.idempotencyReference = Objects.requireNonNull(idempotencyReference, "idempotencyReference must not be null");
        this.retentionPolicyProfile = Objects.requireNonNull(retentionPolicyProfile, "retentionPolicyProfile must not be null");
        this.integrityProof = Objects.requireNonNull(integrityProof, "integrityProof must not be null");
    }

    private static String normalizeRequired(String value, String field, int maxLength) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be at most " + maxLength + " characters");
        }
        return value;
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
