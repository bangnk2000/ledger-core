package com.bangnk.ledgercore.ledger_core.idempotency.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.LifecycleEventRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.LifecycleEventType;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.LifecycleEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaLifecycleEventRepositoryAdapter implements LifecycleEventRepositoryPort {

    private final SharedLifecycleEventJpaRepository repository;

    public JpaLifecycleEventRepositoryAdapter(SharedLifecycleEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public LifecycleEvent save(LifecycleEvent event) {
        repository.save(toEntity(event));
        return event;
    }

    @Override
    public List<LifecycleEvent> findByRecordId(UUID recordId) {
        return repository.findByRecordId(recordId).stream().map(this::toDomain).toList();
    }

    private LifecycleEventJpaEntity toEntity(LifecycleEvent event) {
        LifecycleEventJpaEntity entity = new LifecycleEventJpaEntity();
        entity.setId(event.eventId());
        entity.setRecordId(event.recordId());
        entity.setEventType(event.eventType().name());
        entity.setRecordedAt(event.recordedAt());
        entity.setActorType(event.actorType());
        entity.setActorId(event.actorId());
        entity.setDetails(event.details());
        return entity;
    }

    private LifecycleEvent toDomain(LifecycleEventJpaEntity entity) {
        return new LifecycleEvent(
            entity.getId(),
            entity.getRecordId(),
            LifecycleEventType.valueOf(entity.getEventType()),
            entity.getRecordedAt(),
            entity.getActorType(),
            entity.getActorId(),
            entity.getDetails()
        );
    }
}
