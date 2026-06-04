package com.bangnk.ledgercore.ledger_core.idempotency.application;

import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyCleanupUseCase;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyObservabilityPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyRecordRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.LifecycleEventRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.LifecycleEventType;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.LifecycleEvent;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IdempotencyCleanupService implements IdempotencyCleanupUseCase {

    private final IdempotencyRecordRepositoryPort recordRepository;
    private final LifecycleEventRepositoryPort eventRepository;
    private final IdempotencyObservabilityPort observabilityPort;

    public IdempotencyCleanupService(
        IdempotencyRecordRepositoryPort recordRepository,
        LifecycleEventRepositoryPort eventRepository,
        IdempotencyObservabilityPort observabilityPort
    ) {
        this.recordRepository = recordRepository;
        this.eventRepository = eventRepository;
        this.observabilityPort = observabilityPort;
    }

    @Override
    public int cleanupExpiredRecords(Instant now) {
        for (IdempotencyRecord record : recordRepository.findReplayWindowExpiredRecords(now)) {
            record.refreshRetentionStatus(now);
            recordRepository.save(record);
            eventRepository.save(LifecycleEvent.create(
                record.getId(),
                LifecycleEventType.REPLAY_WINDOW_ELAPSED,
                "SYSTEM",
                "cleanup-service",
                "Replay window elapsed; record tombstoned"
            ));
        }

        int cleanedCount = 0;
        for (IdempotencyRecord record : recordRepository.findPurgeableRecords(now)) {
            record.refreshRetentionStatus(now);
            if (record.getRetentionStatus().name().equals("PURGE_ELIGIBLE")) {
                recordRepository.delete(record);
                cleanedCount++;
                eventRepository.save(LifecycleEvent.create(
                    record.getId(),
                    LifecycleEventType.TOMBSTONE_ELAPSED,
                    "SYSTEM",
                    "cleanup-service",
                    "Tombstone elapsed; record purged"
                ));
            }
        }

        observabilityPort.trackCleanup(cleanedCount);
        return cleanedCount;
    }
}
