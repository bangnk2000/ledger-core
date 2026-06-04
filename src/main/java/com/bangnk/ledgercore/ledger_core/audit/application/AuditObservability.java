package com.bangnk.ledgercore.ledger_core.audit.application;

import com.bangnk.ledgercore.ledger_core.audit.application.port.out.AuditObservabilityPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuditObservability implements AuditObservabilityPort {

    private final Counter captureAcceptedCounter;
    private final Counter publicationDeferredCounter;
    private final Counter investigationQueryCounter;
    private final Counter retentionTransitionCounter;
    private final Counter integrityVerificationCounter;

    public AuditObservability(MeterRegistry meterRegistry) {
        MeterRegistry registry = meterRegistry != null ? meterRegistry : new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        this.captureAcceptedCounter = Counter.builder("audit.capture.accepted").register(registry);
        this.publicationDeferredCounter = Counter.builder("audit.publication.deferred").register(registry);
        this.investigationQueryCounter = Counter.builder("audit.investigation.query").register(registry);
        this.retentionTransitionCounter = Counter.builder("audit.retention.transition").register(registry);
        this.integrityVerificationCounter = Counter.builder("audit.integrity.verification").register(registry);
    }

    @Override
    public void recordCaptureAccepted(UUID eventId) {
        log.info("Audit capture accepted: eventId={}", eventId);
        captureAcceptedCounter.increment();
    }

    @Override
    public void recordPublicationDeferred(UUID eventId, String destinationType) {
        log.info("Audit publication deferred: eventId={}, destinationType={}", eventId, destinationType);
        publicationDeferredCounter.increment();
    }

    @Override
    public void recordInvestigationQuery(String moduleName, String filterType) {
        log.info("Audit investigation query: moduleName={}, filterType={}", moduleName, filterType);
        investigationQueryCounter.increment();
    }

    @Override
    public void recordRetentionTransition(UUID eventId, String transitionType) {
        log.info("Audit retention transition: eventId={}, transitionType={}", eventId, transitionType);
        retentionTransitionCounter.increment();
    }

    @Override
    public void recordIntegrityVerification(UUID eventId, boolean success) {
        log.info("Audit integrity verification: eventId={}, success={}", eventId, success);
        integrityVerificationCounter.increment();
    }
}
