package com.bangnk.ledgercore.ledger_core.idempotency.application;

import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyClaimUseCase;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyObservabilityPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyRecordRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.LifecycleEventRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.IdempotencyState;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.LifecycleEventType;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.LifecycleEvent;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.ClaimOwner;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IdempotencyClaimService implements IdempotencyClaimUseCase {

    private final IdempotencyRecordRepositoryPort recordRepository;
    private final LifecycleEventRepositoryPort eventRepository;
    private final IdempotencyObservabilityPort observabilityPort;
    private final IdempotencyConflictService conflictService;

    public IdempotencyClaimService(
        IdempotencyRecordRepositoryPort recordRepository,
        LifecycleEventRepositoryPort eventRepository,
        IdempotencyObservabilityPort observabilityPort
    ) {
        this.recordRepository = recordRepository;
        this.eventRepository = eventRepository;
        this.observabilityPort = observabilityPort;
        this.conflictService = new IdempotencyConflictService(eventRepository, observabilityPort);
    }

    @Override
    public IdempotencyDecision claim(ClaimCommand command) {
        Optional<IdempotencyRecord> optRecord = recordRepository.findByScopeAndKey(command.scope(), command.key());
        
        if (optRecord.isEmpty()) {
            Instant now = Instant.now();
            Instant replayWindowExpiresAt = now.plus(Duration.ofHours(24));
            Instant tombstoneExpiresAt = now.plus(Duration.ofDays(7));
            
            IdempotencyRecord record = IdempotencyRecord.builder()
                .id(UUID.randomUUID())
                .scope(command.scope())
                .key(command.key().withExpiration(replayWindowExpiresAt, tombstoneExpiresAt))
                .fingerprint(command.fingerprint())
                .state(IdempotencyState.RECEIVED)
                .replayWindowExpiresAt(replayWindowExpiresAt)
                .tombstoneExpiresAt(tombstoneExpiresAt)
                .businessReference(command.businessReference())
                .correlationId(command.correlationId())
                .causationId(command.causationId())
                .attemptCount(1)
                .build();
                
            ClaimOwner owner = ClaimOwner.generate();
            record.claim(owner, now);
            
            if (!recordRepository.createIfAbsent(record)) {
                return recordRepository.findByScopeAndKey(command.scope(), command.key())
                    .map(existingRecord -> resolveExistingRecord(command, existingRecord, now))
                    .orElseThrow(() -> new IdempotencyApplicationErrors.IdempotencyException("Claim winner could not be resolved"));
            }
            
            LifecycleEvent event = LifecycleEvent.create(
                record.getId(),
                LifecycleEventType.CLAIM_GRANTED,
                command.actorType(),
                command.actorId(),
                "First execution claim granted"
            );
            eventRepository.save(event);
            
            observabilityPort.trackClaimGranted(command.scope().scopeType(), command.scope().scopeValue(), command.key().keyValue());
            
            return IdempotencyDecision.firstExecution(owner);
        }
        
        IdempotencyRecord existingRecord = optRecord.get();
        
        existingRecord.setAttemptCount(existingRecord.getAttemptCount() + 1);
        Instant now = Instant.now();
        existingRecord.setLastSeenAt(now);
        recordRepository.save(existingRecord);

        return resolveExistingRecord(command, existingRecord, now);
    }

    private IdempotencyDecision resolveExistingRecord(
        ClaimCommand command,
        IdempotencyRecord existingRecord,
        Instant now
    ) {
        return conflictService.resolveExistingRecord(existingRecord, command, now);
    }
}
