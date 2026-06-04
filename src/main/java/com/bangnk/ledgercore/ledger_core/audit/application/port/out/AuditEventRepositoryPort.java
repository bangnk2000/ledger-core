package com.bangnk.ledgercore.ledger_core.audit.application.port.out;

import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditEventRepositoryPort {
    AuditEvent save(AuditEvent event);

    Optional<AuditEvent> findById(UUID eventId);

    List<AuditEvent> findEligibleForRestrictedRetention(Instant asOf);

    List<AuditEvent> findEligibleForDisposition(Instant asOf);
}
