package com.bangnk.ledgercore.ledger_core.idempotency.application;

import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyClaimUseCase.ClaimCommand;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyObservabilityPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.LifecycleEventRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.IdempotencyState;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.LifecycleEventType;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.LifecycleEvent;
import java.time.Instant;

public class IdempotencyConflictService {

    private final LifecycleEventRepositoryPort eventRepository;
    private final IdempotencyObservabilityPort observabilityPort;

    public IdempotencyConflictService(
        LifecycleEventRepositoryPort eventRepository,
        IdempotencyObservabilityPort observabilityPort
    ) {
        this.eventRepository = eventRepository;
        this.observabilityPort = observabilityPort;
    }

    public IdempotencyDecision resolveExistingRecord(
        IdempotencyRecord existingRecord,
        ClaimCommand command,
        Instant now
    ) {
        if (existingRecord.getFingerprint().conflictsWith(command.fingerprint())) {
            observabilityPort.trackConflictDetected(command.scope().scopeType(), command.scope().scopeValue(), command.key().keyValue());
            eventRepository.save(LifecycleEvent.create(
                existingRecord.getId(),
                LifecycleEventType.CONFLICT_DETECTED,
                command.actorType(),
                command.actorId(),
                "Fingerprint mismatch"
            ));
            return IdempotencyDecision.conflict("Fingerprint mismatch");
        }

        return switch (existingRecord.getState()) {
            case CLAIMED, PROCESSING -> {
                observabilityPort.trackDuplicateInProgress(command.scope().scopeType(), command.scope().scopeValue(), command.key().keyValue());
                eventRepository.save(LifecycleEvent.create(
                    existingRecord.getId(),
                    LifecycleEventType.DUPLICATE_SEEN,
                    command.actorType(),
                    command.actorId(),
                    "Duplicate in progress seen"
                ));
                yield IdempotencyDecision.duplicateInProgress();
            }
            case COMPLETED -> {
                if (existingRecord.getReplayWindowExpiresAt() != null && now.isAfter(existingRecord.getReplayWindowExpiresAt())) {
                    observabilityPort.trackExpiration(command.scope().scopeType(), command.scope().scopeValue(), command.key().keyValue());
                    yield IdempotencyDecision.expiredKey();
                }
                observabilityPort.trackDuplicateSeen(command.scope().scopeType(), command.scope().scopeValue(), command.key().keyValue());
                eventRepository.save(LifecycleEvent.create(
                    existingRecord.getId(),
                    LifecycleEventType.DUPLICATE_SEEN,
                    command.actorType(),
                    command.actorId(),
                    "Duplicate execution replayed"
                ));
                yield IdempotencyDecision.replay(existingRecord.getReplayOutcome());
            }
            case INDETERMINATE -> {
                observabilityPort.trackIndeterminateRecorded(command.scope().scopeType(), command.scope().scopeValue(), command.key().keyValue());
                yield IdempotencyDecision.indeterminate(existingRecord.getReplayOutcome());
            }
            case REJECTED, RECEIVED -> IdempotencyDecision.conflict("Prior request execution failed or rejected");
        };
    }
}
