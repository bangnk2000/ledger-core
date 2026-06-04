package com.bangnk.ledgercore.ledger_core.audit.application;

import com.bangnk.ledgercore.ledger_core.audit.application.port.in.AuditPublicationRecoveryUseCase;
import com.bangnk.ledgercore.ledger_core.audit.application.port.out.AuditObservabilityPort;
import com.bangnk.ledgercore.ledger_core.audit.application.port.out.AuditPublicationRepositoryPort;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AuditPublicationRecoveryService implements AuditPublicationRecoveryUseCase {

    private final AuditPublicationRepositoryPort publicationRepository;
    private final AuditObservabilityPort observabilityPort;

    public AuditPublicationRecoveryService(
        AuditPublicationRepositoryPort publicationRepository,
        AuditObservabilityPort observabilityPort
    ) {
        this.publicationRepository = publicationRepository;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public PublicationRecoveryResult recoverPending(Instant asOf) {
        var attempts = publicationRepository.findPendingAttempts(asOf);
        attempts.forEach(attempt -> observabilityPort.recordPublicationDeferred(attempt.getEventId(), attempt.getDestinationType()));
        return new PublicationRecoveryResult(
            attempts.size(),
            attempts.isEmpty()
                ? AuditDecisionTypes.PublicationRecoveryDecisionType.NO_PENDING_PUBLICATION
                : AuditDecisionTypes.PublicationRecoveryDecisionType.RETRIED
        );
    }
}
