package com.bangnk.ledgercore.ledger_core.idempotency.application;

import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyInspectionUseCase;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyRecordRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.LifecycleEventRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.ReplayOutcomeRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyKey;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyScope;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IdempotencyInspectionService implements IdempotencyInspectionUseCase {

    private final IdempotencyRecordRepositoryPort recordRepository;
    private final LifecycleEventRepositoryPort eventRepository;
    private final ReplayOutcomeRepositoryPort outcomeRepository;

    public IdempotencyInspectionService(
        IdempotencyRecordRepositoryPort recordRepository,
        LifecycleEventRepositoryPort eventRepository,
        ReplayOutcomeRepositoryPort outcomeRepository
    ) {
        this.recordRepository = recordRepository;
        this.eventRepository = eventRepository;
        this.outcomeRepository = outcomeRepository;
    }

    @Override
    public Optional<IdempotencyRecord> inspectRecord(UUID recordId) {
        return recordRepository.findById(recordId).map(this::enrichRecord);
    }

    @Override
    public Optional<IdempotencyRecord> inspectRecord(IdempotencyScope scope, IdempotencyKey key) {
        return recordRepository.findByScopeAndKey(scope, key).map(this::enrichRecord);
    }

    private IdempotencyRecord enrichRecord(IdempotencyRecord record) {
        outcomeRepository.findByRecordId(record.getId()).ifPresent(record::setReplayOutcome);
        record.getLifecycleEvents().clear();
        record.getLifecycleEvents().addAll(eventRepository.findByRecordId(record.getId()));
        return record;
    }
}
