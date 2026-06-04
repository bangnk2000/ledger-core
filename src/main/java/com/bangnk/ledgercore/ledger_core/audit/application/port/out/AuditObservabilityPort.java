package com.bangnk.ledgercore.ledger_core.audit.application.port.out;

import java.util.UUID;

public interface AuditObservabilityPort {
    void recordCaptureAccepted(UUID eventId);

    void recordPublicationDeferred(UUID eventId, String destinationType);

    void recordInvestigationQuery(String moduleName, String filterType);

    void recordRetentionTransition(UUID eventId, String transitionType);

    void recordIntegrityVerification(UUID eventId, boolean success);
}
