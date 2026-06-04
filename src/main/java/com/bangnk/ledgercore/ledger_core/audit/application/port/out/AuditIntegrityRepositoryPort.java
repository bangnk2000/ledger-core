package com.bangnk.ledgercore.ledger_core.audit.application.port.out;

import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IntegrityProof;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuditIntegrityRepositoryPort {
    Optional<IntegrityProof> findIntegrityProof(UUID eventId);

    void updateIntegrityProof(UUID eventId, IntegrityProof integrityProof, Instant verifiedAt);
}
