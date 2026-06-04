package com.bangnk.ledgercore.ledger_core.ledger.application.command;

import com.bangnk.ledgercore.ledger_core.audit.application.AuditCaptureCommand;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.AuditEventType;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.PresenceStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ReferenceStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.ActorIdentity;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.AuditRetentionPolicyProfile;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IdempotencyReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.LedgerTransactionReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.TraceContext;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums;
import java.time.Duration;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LedgerAuditTranslator {

    public AuditCaptureCommand toCaptureCommand(
        PostLedgerTransactionCommand command,
        String stateTo,
        String ledgerTransactionId,
        String idempotencyRecordId,
        String idempotencyKey
    ) {
        boolean hasLedgerTransaction = ledgerTransactionId != null && !ledgerTransactionId.isBlank();
        boolean hasIdempotencyReference = (idempotencyKey != null && !idempotencyKey.isBlank())
            || (idempotencyRecordId != null && !idempotencyRecordId.isBlank());
        return new AuditCaptureCommand(
            AuditEventType.LEDGER_POSTING,
            "v1",
            "ledger",
            command.auditTrace().submittedAt(),
            "ledger-transaction",
            hasLedgerTransaction ? ledgerTransactionId : command.businessReference(),
            command.businessReference(),
            null,
            stateTo,
            Map.of("entryCount", command.entries().size(), "description", command.description()),
            new ActorIdentity(
                mapActorType(command.auditTrace().actor().actorType()),
                command.auditTrace().actor().actorId(),
                "ledger-core",
                null,
                PresenceStatus.KNOWN
            ),
            new TraceContext(
                command.auditTrace().correlationId(),
                command.requestIdentity().requestId(),
                command.auditTrace().causationId(),
                PresenceStatus.KNOWN
            ),
            new LedgerTransactionReference(
                hasLedgerTransaction ? ledgerTransactionId : null,
                hasLedgerTransaction ? "POSTING" : null,
                hasLedgerTransaction ? ReferenceStatus.PRESENT : ReferenceStatus.ABSENT
            ),
            new IdempotencyReference(
                hasIdempotencyReference ? idempotencyKey : null,
                hasIdempotencyReference ? idempotencyRecordId : null,
                hasIdempotencyReference ? "requesterScope" : null,
                hasIdempotencyReference ? command.requestIdentity().requesterScope() : null,
                hasIdempotencyReference ? ReferenceStatus.PRESENT : ReferenceStatus.ABSENT
            ),
            new AuditRetentionPolicyProfile("DEFAULT", Duration.ofDays(30), Duration.ofDays(365), "RESTRICT", "FINANCIAL")
        );
    }

    private ActorType mapActorType(LedgerEnums.ActorType actorType) {
        return switch (actorType) {
            case USER -> ActorType.USER;
            case SYSTEM -> ActorType.SYSTEM;
            case SERVICE -> ActorType.SERVICE;
        };
    }
}
