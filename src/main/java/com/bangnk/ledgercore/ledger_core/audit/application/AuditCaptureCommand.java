package com.bangnk.ledgercore.ledger_core.audit.application;

import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.AuditEventType;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.ActorIdentity;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.AuditRetentionPolicyProfile;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IdempotencyReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.LedgerTransactionReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.TraceContext;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record AuditCaptureCommand(
    AuditEventType eventType,
    String schemaVersion,
    String moduleName,
    Instant occurredAt,
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
    AuditRetentionPolicyProfile retentionPolicyProfile
) {
    public AuditCaptureCommand {
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(schemaVersion, "schemaVersion must not be null");
        Objects.requireNonNull(moduleName, "moduleName must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(subjectType, "subjectType must not be null");
        Objects.requireNonNull(subjectId, "subjectId must not be null");
        safeDetails = safeDetails == null ? Map.of() : Map.copyOf(safeDetails);
        Objects.requireNonNull(actorIdentity, "actorIdentity must not be null");
        Objects.requireNonNull(traceContext, "traceContext must not be null");
        Objects.requireNonNull(ledgerTransactionReference, "ledgerTransactionReference must not be null");
        Objects.requireNonNull(idempotencyReference, "idempotencyReference must not be null");
        Objects.requireNonNull(retentionPolicyProfile, "retentionPolicyProfile must not be null");
    }
}
