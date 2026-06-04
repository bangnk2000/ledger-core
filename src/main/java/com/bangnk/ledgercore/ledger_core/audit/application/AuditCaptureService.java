package com.bangnk.ledgercore.ledger_core.audit.application;

import com.bangnk.ledgercore.ledger_core.audit.application.port.in.AuditCaptureUseCase;
import com.bangnk.ledgercore.ledger_core.audit.application.port.out.AuditEventRepositoryPort;
import com.bangnk.ledgercore.ledger_core.audit.application.port.out.AuditObservabilityPort;
import com.bangnk.ledgercore.ledger_core.audit.application.port.out.AuditPublicationRepositoryPort;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.IntegrityVerificationStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.PublicationResult;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEvent;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.PublicationAttempt;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IntegrityProof;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditCaptureService implements AuditCaptureUseCase {

    static final String INTERNAL_BACKLOG = "INTERNAL_BACKLOG";

    private final AuditEventRepositoryPort eventRepository;
    private final AuditPublicationRepositoryPort publicationRepository;
    private final AuditObservabilityPort observabilityPort;
    private final Clock clock;

    public AuditCaptureService(
        AuditEventRepositoryPort eventRepository,
        AuditPublicationRepositoryPort publicationRepository,
        AuditObservabilityPort observabilityPort,
        Clock clock
    ) {
        this.eventRepository = eventRepository;
        this.publicationRepository = publicationRepository;
        this.observabilityPort = observabilityPort;
        this.clock = clock;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CaptureResult capture(AuditCaptureCommand command) {
        Instant now = Instant.now(clock);
        AuditEvent event = AuditEvent.builder()
            .eventType(command.eventType())
            .schemaVersion(command.schemaVersion())
            .moduleName(command.moduleName())
            .occurredAt(command.occurredAt())
            .capturedAt(now)
            .subjectType(command.subjectType())
            .subjectId(command.subjectId())
            .businessReference(command.businessReference())
            .stateFrom(command.stateFrom())
            .stateTo(command.stateTo())
            .safeDetails(command.safeDetails())
            .actorIdentity(command.actorIdentity())
            .traceContext(command.traceContext())
            .ledgerTransactionReference(command.ledgerTransactionReference())
            .idempotencyReference(command.idempotencyReference())
            .retentionPolicyProfile(command.retentionPolicyProfile())
            .integrityProof(new IntegrityProof(
                "v1",
                digestFor(command),
                null,
                null,
                IntegrityVerificationStatus.UNVERIFIED
            ))
            .build();

        AuditEvent stored = eventRepository.save(event);
        publicationRepository.saveAttempt(PublicationAttempt.builder()
            .eventId(stored.getId())
            .destinationType(INTERNAL_BACKLOG)
            .attemptedAt(now)
            .result(PublicationResult.DEFERRED)
            .nextRetryAt(now)
            .build());

        observabilityPort.recordCaptureAccepted(stored.getId());
        observabilityPort.recordPublicationDeferred(stored.getId(), INTERNAL_BACKLOG);
        return new CaptureResult(stored.getId(), AuditDecisionTypes.CaptureDecisionType.CAPTURED_WITH_DEFERRED_PUBLICATION);
    }

    private String digestFor(AuditCaptureCommand command) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String canonical = String.join("|",
                command.eventType().name(),
                command.schemaVersion(),
                command.moduleName(),
                command.subjectType(),
                command.subjectId(),
                String.valueOf(command.businessReference()),
                String.valueOf(command.stateFrom()),
                String.valueOf(command.stateTo()),
                command.traceContext().correlationId() == null ? "" : command.traceContext().correlationId(),
                command.traceContext().requestId() == null ? "" : command.traceContext().requestId()
            );
            return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new AuditApplicationErrors.AuditCaptureException("Unable to compute audit integrity digest");
        }
    }
}
