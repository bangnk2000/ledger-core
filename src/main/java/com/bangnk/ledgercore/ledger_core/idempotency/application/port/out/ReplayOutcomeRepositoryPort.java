package com.bangnk.ledgercore.ledger_core.idempotency.application.port.out;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.ReplayOutcome;
import java.util.Optional;
import java.util.UUID;

public interface ReplayOutcomeRepositoryPort {
    ReplayOutcome save(UUID recordId, ReplayOutcome outcome);
    Optional<ReplayOutcome> findByRecordId(UUID recordId);
}
