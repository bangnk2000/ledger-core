package com.bangnk.ledgercore.ledger_core.audit.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.audit.application.port.out.AuditPublicationRepositoryPort;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.PublicationResult;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.PublicationAttempt;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JpaAuditPublicationRepositoryAdapter implements AuditPublicationRepositoryPort {

    private final SharedPublicationAttemptJpaRepository repository;

    public JpaAuditPublicationRepositoryAdapter(SharedPublicationAttemptJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PublicationAttempt saveAttempt(PublicationAttempt attempt) {
        repository.saveAndFlush(toEntity(attempt));
        return attempt;
    }

    @Override
    public List<PublicationAttempt> findPendingAttempts(Instant asOf) {
        return repository.findByResultAndNextRetryAtLessThanEqual(PublicationResult.DEFERRED.name(), asOf)
            .stream()
            .map(this::toDomain)
            .toList();
    }

    private PublicationAttemptJpaEntity toEntity(PublicationAttempt attempt) {
        PublicationAttemptJpaEntity entity = new PublicationAttemptJpaEntity();
        entity.setId(attempt.getId());
        entity.setEventId(attempt.getEventId());
        entity.setDestinationType(attempt.getDestinationType());
        entity.setAttemptedAt(attempt.getAttemptedAt());
        entity.setResult(attempt.getResult().name());
        entity.setFailureReason(attempt.getFailureReason());
        entity.setNextRetryAt(attempt.getNextRetryAt());
        return entity;
    }

    private PublicationAttempt toDomain(PublicationAttemptJpaEntity entity) {
        return PublicationAttempt.builder()
            .id(entity.getId())
            .eventId(entity.getEventId())
            .destinationType(entity.getDestinationType())
            .attemptedAt(entity.getAttemptedAt())
            .result(PublicationResult.valueOf(entity.getResult()))
            .failureReason(entity.getFailureReason())
            .nextRetryAt(entity.getNextRetryAt())
            .build();
    }
}
