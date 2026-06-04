package com.bangnk.ledgercore.ledger_core.idempotency.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyClaimUseCase.ClaimCommand;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyObservabilityPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.LifecycleEventRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.IdempotencyState;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.OutcomeType;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.ReplayOutcome;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.ClaimOwner;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyKey;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyScope;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.RequestFingerprint;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdempotencyConflictServiceTest {

    private LifecycleEventRepositoryPort eventRepository;
    private IdempotencyObservabilityPort observabilityPort;
    private IdempotencyConflictService service;

    @BeforeEach
    void setUp() {
        eventRepository = mock(LifecycleEventRepositoryPort.class);
        observabilityPort = mock(IdempotencyObservabilityPort.class);
        service = new IdempotencyConflictService(eventRepository, observabilityPort);
    }

    @Test
    void returnsConflictForDifferentFingerprintReuse() {
        IdempotencyRecord existing = completedRecord(new RequestFingerprint("hash-1", "v1", "amount=100", "profile"));
        ClaimCommand command = command(new RequestFingerprint("hash-2", "v1", "amount=250", "profile"));

        IdempotencyDecision decision = service.resolveExistingRecord(existing, command, Instant.now());

        assertEquals(IdempotencyDecisionTypes.CONFLICT, decision.decisionType());
        verify(eventRepository).save(any());
        verify(observabilityPort).trackConflictDetected("tenant", "scope-1", "key-1");
    }

    @Test
    void returnsDuplicateInProgressWhenRecordIsClaimed() {
        IdempotencyRecord existing = IdempotencyRecord.builder()
            .scope(scope())
            .key(key())
            .fingerprint(fingerprint())
            .state(IdempotencyState.CLAIMED)
            .claimOwner(ClaimOwner.generate())
            .build();

        IdempotencyDecision decision = service.resolveExistingRecord(existing, command(fingerprint()), Instant.now());

        assertEquals(IdempotencyDecisionTypes.DUPLICATE_IN_PROGRESS, decision.decisionType());
        verify(observabilityPort).trackDuplicateInProgress("tenant", "scope-1", "key-1");
    }

    @Test
    void returnsReplayForCompletedMatchingRecord() {
        ReplayOutcome outcome = ReplayOutcome.create(OutcomeType.FIRST_EXECUTION, "200", "{\"ok\":true}", 200, "tx-1");
        IdempotencyRecord existing = IdempotencyRecord.builder()
            .scope(scope())
            .key(key())
            .fingerprint(fingerprint())
            .state(IdempotencyState.COMPLETED)
            .replayOutcome(outcome)
            .replayWindowExpiresAt(Instant.now().plus(Duration.ofHours(1)))
            .build();

        IdempotencyDecision decision = service.resolveExistingRecord(existing, command(fingerprint()), Instant.now());

        assertEquals(IdempotencyDecisionTypes.REPLAY, decision.decisionType());
        assertSame(outcome, decision.replayOutcome());
        verify(observabilityPort).trackDuplicateSeen("tenant", "scope-1", "key-1");
    }

    private static IdempotencyRecord completedRecord(RequestFingerprint fingerprint) {
        return IdempotencyRecord.builder()
            .scope(scope())
            .key(key())
            .fingerprint(fingerprint)
            .state(IdempotencyState.COMPLETED)
            .replayWindowExpiresAt(Instant.now().plus(Duration.ofHours(1)))
            .build();
    }

    private static ClaimCommand command(RequestFingerprint fingerprint) {
        return new ClaimCommand(scope(), key(), fingerprint, "business-ref", "SYSTEM", "svc-1", "corr-1", null);
    }

    private static IdempotencyScope scope() {
        return new IdempotencyScope("tenant", "scope-1", "ledger-posting", "DEFAULT");
    }

    private static IdempotencyKey key() {
        return new IdempotencyKey("key-1");
    }

    private static RequestFingerprint fingerprint() {
        return new RequestFingerprint("hash-1", "v1", "amount=100", "profile");
    }
}
