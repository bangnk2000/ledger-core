package com.bangnk.ledgercore.ledger_core.audit.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.AuditEventType;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.IntegrityVerificationStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.PresenceStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ReferenceStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEvent;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.ActorIdentity;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.AuditRetentionPolicyProfile;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IdempotencyReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IntegrityProof;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.LedgerTransactionReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.TraceContext;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuditEventTest {

    @Test
    void createsImmutableAuditEventWithExplicitAbsentReferences() {
        Map<String, Object> details = new HashMap<>();
        details.put("amount", "100.0000");

        AuditEvent event = AuditEvent.builder()
            .eventType(AuditEventType.STATE_TRANSITION)
            .schemaVersion("v1")
            .moduleName("ledger")
            .occurredAt(Instant.parse("2026-06-04T00:00:00Z"))
            .subjectType("ledger-transaction")
            .subjectId("txn-1")
            .safeDetails(details)
            .actorIdentity(new ActorIdentity(ActorType.SYSTEM, "svc-ledger", "ledger-core", null, PresenceStatus.KNOWN))
            .traceContext(new TraceContext("corr-1", "req-1", null, PresenceStatus.KNOWN))
            .ledgerTransactionReference(new LedgerTransactionReference(null, null, ReferenceStatus.ABSENT))
            .idempotencyReference(new IdempotencyReference(null, null, null, null, ReferenceStatus.ABSENT))
            .retentionPolicyProfile(new AuditRetentionPolicyProfile("DEFAULT", Duration.ofDays(30), Duration.ofDays(365), "RESTRICT", "FINANCIAL"))
            .integrityProof(new IntegrityProof("v1", "digest-1", null, null, IntegrityVerificationStatus.UNVERIFIED))
            .build();

        details.put("tampered", true);

        assertEquals("100.0000", event.getSafeDetails().get("amount"));
        assertTrue(event.getSafeDetails().containsKey("amount"));
        assertEquals(ReferenceStatus.ABSENT, event.getLedgerTransactionReference().referenceStatus());
        assertEquals(ReferenceStatus.ABSENT, event.getIdempotencyReference().referenceStatus());
    }

    @Test
    void rejectsKnownActorWithoutActorId() {
        assertThrows(IllegalArgumentException.class, () -> new ActorIdentity(
            ActorType.USER,
            null,
            "portal",
            null,
            PresenceStatus.KNOWN
        ));
    }

    @Test
    void rejectsPresentIdempotencyReferenceWithoutIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> new IdempotencyReference(
            null,
            null,
            "tenant",
            "tenant-a",
            ReferenceStatus.PRESENT
        ));
    }
}
