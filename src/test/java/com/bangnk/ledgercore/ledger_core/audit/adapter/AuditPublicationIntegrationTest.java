package com.bangnk.ledgercore.ledger_core.audit.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = com.bangnk.ledgercore.ledger_core.LedgerCoreApplication.class)
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext
class AuditPublicationIntegrationTest {

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
    void returnsOnlyDeferredAttemptsEligibleForRetry() {
        AuditEvent event = auditEventRepository.save(AuditEvent.builder()
            .eventType(AuditEventType.PUBLICATION_OUTCOME)
            .schemaVersion("v1")
            .moduleName("audit")
            .occurredAt(Instant.parse("2026-06-04T11:00:00Z"))
            .capturedAt(Instant.parse("2026-06-04T11:00:01Z"))
            .subjectType("audit-event")
            .subjectId("event-1")
            .safeDetails(java.util.Map.of("destination", "backlog"))
            .actorIdentity(new ActorIdentity(ActorType.SYSTEM, "audit-system", "ledger-core", null, PresenceStatus.KNOWN))
            .traceContext(new TraceContext("corr-1", "req-1", null, PresenceStatus.KNOWN))
            .ledgerTransactionReference(new LedgerTransactionReference(null, null, ReferenceStatus.ABSENT))
            .idempotencyReference(new IdempotencyReference(null, null, null, null, ReferenceStatus.ABSENT))
            .retentionPolicyProfile(new AuditRetentionPolicyProfile("DEFAULT", Duration.ofDays(30), Duration.ofDays(365), "RESTRICT", "FINANCIAL"))
            .integrityProof(new IntegrityProof("v1", "digest-2", null, null, IntegrityVerificationStatus.UNVERIFIED))
            .build());

        publicationRepository.saveAttempt(PublicationAttempt.builder()
            .eventId(event.getId())
            .destinationType("INTERNAL_BACKLOG")
            .attemptedAt(Instant.parse("2026-06-04T11:00:02Z"))
            .result(PublicationResult.DEFERRED)
            .nextRetryAt(Instant.parse("2026-06-04T11:05:00Z"))
            .build());
        publicationRepository.saveAttempt(PublicationAttempt.builder()
            .eventId(event.getId())
            .destinationType("DOWNSTREAM")
            .attemptedAt(Instant.parse("2026-06-04T11:00:03Z"))
            .result(PublicationResult.FAILED)
            .failureReason("offline")
            .nextRetryAt(Instant.parse("2026-06-04T11:05:00Z"))
            .build());

        assertEquals(1, publicationRepository.findPendingAttempts(Instant.parse("2026-06-04T11:05:00Z")).size());
    }
}
