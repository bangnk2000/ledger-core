package com.bangnk.ledgercore.ledger_core.balance.application;

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
import com.bangnk.ledgercore.ledger_core.balance.application.command.BalanceMutationRequest;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceActorType;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BalanceAuditTranslator {

    public AuditCaptureCommand toCaptureCommand(
        BalanceMutationRequest request,
        String stateTo,
        String ledgerTransactionId,
        String idempotencyRecordId,
        String idempotencyKey
    ) {
        boolean hasLedgerTransaction = ledgerTransactionId != null && !ledgerTransactionId.isBlank();
        boolean hasIdempotencyReference = (idempotencyKey != null && !idempotencyKey.isBlank())
            || (idempotencyRecordId != null && !idempotencyRecordId.isBlank());
        return new AuditCaptureCommand(
            AuditEventType.STATE_TRANSITION,
            "v1",
            "balance",
            request.expiresAt(),
            "balance-mutation",
            request.businessReference(),
            request.businessReference(),
            null,
            stateTo,
            Map.of(
                "mutationType", request.mutationType().name(),
                "accountCount", request.accountIds().size(),
                "currency", request.currency().value()
            ),
            new ActorIdentity(
                mapActorType(request.actorContext().actorType()),
                request.actorContext().actorId(),
                "ledger-core",
                null,
                PresenceStatus.KNOWN
            ),
            new TraceContext(
                request.actorContext().correlationId(),
                request.requestIdentity().requestId(),
                request.actorContext().causationId(),
                request.actorContext().correlationId() == null && request.requestIdentity().requestId() == null
                    ? PresenceStatus.UNAVAILABLE
                    : PresenceStatus.KNOWN
            ),
            new LedgerTransactionReference(
                hasLedgerTransaction ? ledgerTransactionId : null,
                hasLedgerTransaction ? "BALANCE_MUTATION" : null,
                hasLedgerTransaction ? ReferenceStatus.PRESENT : ReferenceStatus.ABSENT
            ),
            new IdempotencyReference(
                hasIdempotencyReference ? idempotencyKey : null,
                hasIdempotencyReference ? idempotencyRecordId : null,
                hasIdempotencyReference ? "requesterScope" : null,
                hasIdempotencyReference ? request.requestIdentity().requesterScope() : null,
                hasIdempotencyReference ? ReferenceStatus.PRESENT : ReferenceStatus.ABSENT
            ),
            new AuditRetentionPolicyProfile("DEFAULT", Duration.ofDays(30), Duration.ofDays(365), "RESTRICT", "FINANCIAL")
        );
    }

    public AuditCaptureCommand toLifecycleCaptureCommand(
        UUID reservationId,
        RequestIdentity requestIdentity,
        ActorContext actorContext,
        BalanceMutationType mutationType,
        Instant occurredAt,
        String stateTo,
        String ledgerTransactionId,
        String idempotencyRecordId,
        String idempotencyKey
    ) {
        boolean hasLedgerTransaction = ledgerTransactionId != null && !ledgerTransactionId.isBlank();
        boolean hasIdempotencyReference = (idempotencyKey != null && !idempotencyKey.isBlank())
            || (idempotencyRecordId != null && !idempotencyRecordId.isBlank());
        return new AuditCaptureCommand(
            AuditEventType.STATE_TRANSITION,
            "v1",
            "balance",
            occurredAt,
            "balance-mutation",
            reservationId.toString(),
            reservationId.toString(),
            null,
            stateTo,
            Map.of("mutationType", mutationType.name(), "reservationId", reservationId.toString()),
            new ActorIdentity(
                mapActorType(actorContext.actorType()),
                actorContext.actorId(),
                "ledger-core",
                null,
                PresenceStatus.KNOWN
            ),
            new TraceContext(
                actorContext.correlationId(),
                requestIdentity.requestId(),
                actorContext.causationId(),
                actorContext.correlationId() == null && requestIdentity.requestId() == null
                    ? PresenceStatus.UNAVAILABLE
                    : PresenceStatus.KNOWN
            ),
            new LedgerTransactionReference(
                hasLedgerTransaction ? ledgerTransactionId : null,
                hasLedgerTransaction ? mutationType.name() : null,
                hasLedgerTransaction ? ReferenceStatus.PRESENT : ReferenceStatus.ABSENT
            ),
            new IdempotencyReference(
                hasIdempotencyReference ? idempotencyKey : null,
                hasIdempotencyReference ? idempotencyRecordId : null,
                hasIdempotencyReference ? "requesterScope" : null,
                hasIdempotencyReference ? requestIdentity.requesterScope() : null,
                hasIdempotencyReference ? ReferenceStatus.PRESENT : ReferenceStatus.ABSENT
            ),
            new AuditRetentionPolicyProfile("DEFAULT", Duration.ofDays(30), Duration.ofDays(365), "RESTRICT", "FINANCIAL")
        );
    }

    private ActorType mapActorType(BalanceActorType actorType) {
        return switch (actorType) {
            case USER -> ActorType.USER;
            case SYSTEM -> ActorType.SYSTEM;
            case SERVICE -> ActorType.SERVICE;
        };
    }
}
