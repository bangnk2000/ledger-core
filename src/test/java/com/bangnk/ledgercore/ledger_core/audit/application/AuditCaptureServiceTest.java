package com.bangnk.ledgercore.ledger_core.audit.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bangnk.ledgercore.ledger_core.audit.application.port.in.AuditCaptureUseCase.CaptureResult;
import com.bangnk.ledgercore.ledger_core.audit.application.port.out.AuditEventRepositoryPort;
import com.bangnk.ledgercore.ledger_core.audit.application.port.out.AuditObservabilityPort;
import com.bangnk.ledgercore.ledger_core.audit.application.port.out.AuditPublicationRepositoryPort;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.AuditEventType;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.PresenceStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ReferenceStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEvent;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.PublicationAttempt;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.ActorIdentity;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.AuditRetentionPolicyProfile;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IdempotencyReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IntegrityProof;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.LedgerTransactionReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.TraceContext;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class AuditCaptureServiceTest {

    private AuditEventRepositoryPort eventRepository;
    private AuditPublicationRepositoryPort publicationRepository;
    private AuditObservabilityPort observabilityPort;
    private AuditCaptureService captureService;

    @BeforeEach
    void setUp() {
        eventRepository = mock(AuditEventRepositoryPort.class);
        publicationRepository = mock(AuditPublicationRepositoryPort.class);
        observabilityPort = mock(AuditObservabilityPort.class);
        captureService = new AuditCaptureService(
            eventRepository,
            publicationRepository,
            observabilityPort,
            Clock.fixed(Instant.parse("2026-06-04T10:15:30Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void capturesDurableEventAndQueuesPublicationAttempt() {
        AuditCaptureCommand command = new AuditCaptureCommand(
            AuditEventType.STATE_TRANSITION,
            "v1",
            "ledger",
            Instant.parse("2026-06-04T10:10:00Z"),
            "ledger-transaction",
            "txn-1",
            "business-1",
            "PENDING",
            "POSTED",
            Map.of("entryCount", 2),
            new ActorIdentity(ActorType.SERVICE, "svc-ledger", "ledger-core", null, PresenceStatus.KNOWN),
            new TraceContext("corr-1", "req-1", null, PresenceStatus.KNOWN),
            new LedgerTransactionReference("txn-1", "POSTING", ReferenceStatus.PRESENT),
            new IdempotencyReference("idem-1", "record-1", "tenant", "tenant-a", ReferenceStatus.PRESENT),
            new AuditRetentionPolicyProfile("DEFAULT", Duration.ofDays(30), Duration.ofDays(365), "RESTRICT", "FINANCIAL")
        );

        when(eventRepository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(publicationRepository.saveAttempt(any(PublicationAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaptureResult result = captureService.capture(command);

        assertNotNull(result.eventId());
        assertEquals(AuditDecisionTypes.CaptureDecisionType.CAPTURED_WITH_DEFERRED_PUBLICATION, result.decisionType());
        verify(eventRepository).save(any(AuditEvent.class));
        verify(publicationRepository).saveAttempt(any(PublicationAttempt.class));
        verify(observabilityPort).recordCaptureAccepted(any());
        verify(observabilityPort).recordPublicationDeferred(any(), eq("INTERNAL_BACKLOG"));
    }
}
