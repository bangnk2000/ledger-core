package com.bangnk.ledgercore.ledger_core.audit.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bangnk.ledgercore.ledger_core.audit.application.port.out.AuditEventRepositoryPort;
import com.bangnk.ledgercore.ledger_core.audit.application.port.out.AuditPublicationRepositoryPort;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.AuditEventType;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.IntegrityVerificationStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.PresenceStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.PublicationResult;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ReferenceStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEvent;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.PublicationAttempt;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.ActorIdentity;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.AuditRetentionPolicyProfile;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IdempotencyReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IntegrityProof;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.LedgerTransactionReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.TraceContext;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = com.bangnk.ledgercore.ledger_core.LedgerCoreApplication.class)
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext
class AuditPersistenceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AuditEventRepositoryPort auditEventRepository;

    @Autowired
    private AuditPublicationRepositoryPort publicationRepository;

    @Test
    void persistsImmutableAuditEventAndPublicationAttempt() {
        AuditEvent event = AuditEvent.builder()
            .eventType(AuditEventType.LEDGER_POSTING)
            .schemaVersion("v1")
            .moduleName("ledger")
            .occurredAt(Instant.parse("2026-06-04T10:00:00Z"))
            .capturedAt(Instant.parse("2026-06-04T10:00:01Z"))
            .subjectType("ledger-transaction")
            .subjectId("txn-1")
            .businessReference("business-1")
            .stateFrom("PENDING")
            .stateTo("POSTED")
            .safeDetails(java.util.Map.of("entries", 2))
            .actorIdentity(new ActorIdentity(ActorType.SERVICE, "svc-ledger", "ledger-core", null, PresenceStatus.KNOWN))
            .traceContext(new TraceContext("corr-1", "req-1", null, PresenceStatus.KNOWN))
            .ledgerTransactionReference(new LedgerTransactionReference("txn-1", "POSTING", ReferenceStatus.PRESENT))
            .idempotencyReference(new IdempotencyReference("idem-1", "record-1", "tenant", "tenant-a", ReferenceStatus.PRESENT))
            .retentionPolicyProfile(new AuditRetentionPolicyProfile("DEFAULT", Duration.ofDays(30), Duration.ofDays(365), "RESTRICT", "FINANCIAL"))
            .integrityProof(new IntegrityProof("v1", "digest-1", null, null, IntegrityVerificationStatus.UNVERIFIED))
            .build();

        AuditEvent stored = auditEventRepository.save(event);
        PublicationAttempt attempt = publicationRepository.saveAttempt(PublicationAttempt.builder()
            .eventId(stored.getId())
            .destinationType("INTERNAL_BACKLOG")
            .attemptedAt(Instant.parse("2026-06-04T10:00:02Z"))
            .result(PublicationResult.DEFERRED)
            .nextRetryAt(Instant.parse("2026-06-04T10:05:02Z"))
            .build());

        var loaded = auditEventRepository.findById(stored.getId());
        assertTrue(loaded.isPresent());
        assertEquals("txn-1", loaded.orElseThrow().getLedgerTransactionReference().transactionId());
        assertEquals("idem-1", loaded.orElseThrow().getIdempotencyReference().idempotencyKey());

        List<PublicationAttempt> pending = publicationRepository.findPendingAttempts(Instant.parse("2026-06-04T10:05:02Z"));
        assertEquals(1, pending.size());
        assertEquals(attempt.getId(), pending.getFirst().getId());
    }
}
