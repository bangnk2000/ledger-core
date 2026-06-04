package com.bangnk.ledgercore.ledger_core.idempotency.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.ReplayOutcomeRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.OutcomeType;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.ReplayOutcome;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaReplayOutcomeRepositoryAdapter implements ReplayOutcomeRepositoryPort {

    private final SharedReplayOutcomeJpaRepository repository;

    public JpaReplayOutcomeRepositoryAdapter(SharedReplayOutcomeJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ReplayOutcome save(UUID recordId, ReplayOutcome outcome) {
        repository.save(toEntity(recordId, outcome));
        return outcome;
    }

    @Override
    public Optional<ReplayOutcome> findByRecordId(UUID recordId) {
        return repository.findById(recordId).map(this::toDomain);
    }

    private ReplayOutcomeJpaEntity toEntity(UUID recordId, ReplayOutcome outcome) {
        ReplayOutcomeJpaEntity entity = new ReplayOutcomeJpaEntity();
        entity.setRecordId(recordId);
        entity.setOutcomeType(outcome.outcomeType().name());
        entity.setResponseCode(outcome.responseCode());
        entity.setResponsePayload(outcome.responsePayload());
        entity.setHttpStatusHint(outcome.httpStatusHint());
        entity.setBusinessResultReference(outcome.businessResultReference());
        entity.setFinalizedAt(outcome.finalizedAt());
        return entity;
    }

    private ReplayOutcome toDomain(ReplayOutcomeJpaEntity entity) {
        return new ReplayOutcome(
            OutcomeType.valueOf(entity.getOutcomeType()),
            entity.getResponseCode(),
            entity.getResponsePayload(),
            entity.getHttpStatusHint(),
            entity.getBusinessResultReference(),
            entity.getFinalizedAt()
        );
    }
}
