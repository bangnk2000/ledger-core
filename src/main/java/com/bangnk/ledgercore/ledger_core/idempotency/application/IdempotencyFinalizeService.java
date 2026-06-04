package com.bangnk.ledgercore.ledger_core.idempotency.application;

import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyFinalizeUseCase;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyRecordRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.LifecycleEventRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.ReplayOutcomeRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.IdempotencyState;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.LifecycleEventType;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.LifecycleEvent;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.ReplayOutcome;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IdempotencyFinalizeService implements IdempotencyFinalizeUseCase {

    private final IdempotencyRecordRepositoryPort recordRepository;
    private final LifecycleEventRepositoryPort eventRepository;
    private final ReplayOutcomeRepositoryPort outcomeRepository;

    public IdempotencyFinalizeService(
        IdempotencyRecordRepositoryPort recordRepository,
        LifecycleEventRepositoryPort eventRepository,
        ReplayOutcomeRepositoryPort outcomeRepository
    ) {
        this.recordRepository = recordRepository;
        this.eventRepository = eventRepository;
        this.outcomeRepository = outcomeRepository;
    }

    @Override
    public void finalizeClaim(FinalizeCommand command) {
        Optional<IdempotencyRecord> optRecord = recordRepository.findById(command.recordId());
        
        if (optRecord.isEmpty()) {
            throw new IdempotencyApplicationErrors.ClaimNotFoundException("Claim record not found: " + command.recordId());
        }
        
        IdempotencyRecord record = optRecord.get();
        
        // Idempotent finalization check
        if (record.getState() == IdempotencyState.COMPLETED || record.getState() == IdempotencyState.INDETERMINATE) {
            ReplayOutcome existingOutcome = record.getReplayOutcome();
            if (existingOutcome != null &&
                Objects.equals(existingOutcome.responseCode(), command.responseCode()) &&
                Objects.equals(existingOutcome.responsePayload(), command.responsePayload()) &&
                Objects.equals(existingOutcome.businessResultReference(), command.businessResultReference())) {
                // Idempotent call - do nothing
                return;
            } else {
                throw new IdempotencyApplicationErrors.DuplicateFinalizationException(
                    "Record is already finalized with a different outcome."
                );
            }
        }
        
        // Owner verification
        if (record.getClaimOwner() == null || !record.getClaimOwner().equals(command.claimOwner())) {
            throw new IdempotencyApplicationErrors.ClaimOwnerMismatchException(
                "Claim owner mismatch. Active owner: " + record.getClaimOwner() + ", Provided owner: " + command.claimOwner()
            );
        }
        
        ReplayOutcome replayOutcome = new ReplayOutcome(
            command.outcomeType(),
            command.responseCode(),
            command.responsePayload(),
            null, // httpStatusHint can be set as null or derived
            command.businessResultReference(),
            command.finalizedAt() != null ? command.finalizedAt() : java.time.Instant.now()
        );
        
        record.finalizeRecord(command.claimOwner(), replayOutcome);
        
        recordRepository.save(record);
        outcomeRepository.save(record.getId(), replayOutcome);
        
        LifecycleEventType eventType = command.outcomeType() == com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.OutcomeType.INDETERMINATE
            ? LifecycleEventType.INDETERMINATE_RECORDED
            : LifecycleEventType.COMPLETED;
            
        LifecycleEvent event = LifecycleEvent.create(
            record.getId(),
            eventType,
            "SYSTEM",
            "SERVICE",
            "Claim finalized with outcome code: " + command.responseCode()
        );
        eventRepository.save(event);
    }
}
