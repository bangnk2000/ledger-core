package com.bangnk.ledgercore.ledger_core.idempotency.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyClaimUseCase.ClaimCommand;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyFinalizeUseCase;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyObservabilityPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyRecordRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.LifecycleEventRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.ReplayOutcomeRepositoryPort;
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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IdempotencyClaimServiceTest {

    private IdempotencyRecordRepositoryPort recordRepository;
    private LifecycleEventRepositoryPort eventRepository;
    private ReplayOutcomeRepositoryPort outcomeRepository;
    private IdempotencyObservabilityPort observabilityPort;
    private IdempotencyClaimService claimService;
    private IdempotencyFinalizeService finalizeService;

    @BeforeEach
    public void setUp() {
        recordRepository = mock(IdempotencyRecordRepositoryPort.class);
        eventRepository = mock(LifecycleEventRepositoryPort.class);
        outcomeRepository = mock(ReplayOutcomeRepositoryPort.class);
        observabilityPort = mock(IdempotencyObservabilityPort.class);
        claimService = new IdempotencyClaimService(recordRepository, eventRepository, observabilityPort);
        finalizeService = new IdempotencyFinalizeService(recordRepository, eventRepository, outcomeRepository);
    }

    @Test
    public void testFirstExecution() {
        IdempotencyScope scope = new IdempotencyScope("tenant", "123", "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-1", "v1", "summary", "default");
        ClaimCommand command = new ClaimCommand(scope, key, fingerprint, "ref-1", "SYSTEM", "actor-1", "corr-1", "caus-1");

        when(recordRepository.findByScopeAndKey(scope, key)).thenReturn(Optional.empty());
        when(recordRepository.createIfAbsent(any(IdempotencyRecord.class))).thenReturn(true);

        IdempotencyDecision decision = claimService.claim(command);

        assertEquals(IdempotencyDecisionTypes.FIRST_EXECUTION, decision.decisionType());
        assertNotNull(decision.claimOwner());
        verify(recordRepository).createIfAbsent(any(IdempotencyRecord.class));
        verify(eventRepository).save(any());
        verify(observabilityPort).trackClaimGranted(any(), any(), any());
    }

    @Test
    public void testDuplicateInProgress() {
        IdempotencyScope scope = new IdempotencyScope("tenant", "123", "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-1", "v1", "summary", "default");
        ClaimCommand command = new ClaimCommand(scope, key, fingerprint, "ref-1", "SYSTEM", "actor-1", "corr-1", "caus-1");

        IdempotencyRecord existingRecord = IdempotencyRecord.builder()
            .scope(scope)
            .key(key)
            .fingerprint(fingerprint)
            .state(IdempotencyState.CLAIMED)
            .claimOwner(ClaimOwner.generate())
            .build();

        when(recordRepository.findByScopeAndKey(scope, key)).thenReturn(Optional.of(existingRecord));

        IdempotencyDecision decision = claimService.claim(command);

        assertEquals(IdempotencyDecisionTypes.DUPLICATE_IN_PROGRESS, decision.decisionType());
        verify(observabilityPort).trackDuplicateInProgress(any(), any(), any());
    }

    @Test
    public void testReplaySuccess() {
        IdempotencyScope scope = new IdempotencyScope("tenant", "123", "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-1", "v1", "summary", "default");
        ClaimCommand command = new ClaimCommand(scope, key, fingerprint, "ref-1", "SYSTEM", "actor-1", "corr-1", "caus-1");

        ReplayOutcome replayOutcome = ReplayOutcome.create(OutcomeType.REPLAY, "SUCCESS", "payload", 200, "tx-123");
        IdempotencyRecord existingRecord = IdempotencyRecord.builder()
            .scope(scope)
            .key(key)
            .fingerprint(fingerprint)
            .state(IdempotencyState.COMPLETED)
            .replayWindowExpiresAt(Instant.now().plus(Duration.ofHours(1)))
            .replayOutcome(replayOutcome)
            .build();

        when(recordRepository.findByScopeAndKey(scope, key)).thenReturn(Optional.of(existingRecord));

        IdempotencyDecision decision = claimService.claim(command);

        assertEquals(IdempotencyDecisionTypes.REPLAY, decision.decisionType());
        assertEquals(replayOutcome, decision.replayOutcome());
        verify(observabilityPort).trackDuplicateSeen(any(), any(), any());
    }

    @Test
    public void testConflictMismatch() {
        IdempotencyScope scope = new IdempotencyScope("tenant", "123", "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-1");
        RequestFingerprint fingerprint1 = new RequestFingerprint("hash-1", "v1", "summary", "default");
        RequestFingerprint fingerprint2 = new RequestFingerprint("hash-2", "v1", "summary", "default");
        ClaimCommand command = new ClaimCommand(scope, key, fingerprint2, "ref-1", "SYSTEM", "actor-1", "corr-1", "caus-1");

        IdempotencyRecord existingRecord = IdempotencyRecord.builder()
            .scope(scope)
            .key(key)
            .fingerprint(fingerprint1)
            .state(IdempotencyState.COMPLETED)
            .build();

        when(recordRepository.findByScopeAndKey(scope, key)).thenReturn(Optional.of(existingRecord));

        IdempotencyDecision decision = claimService.claim(command);

        assertEquals(IdempotencyDecisionTypes.CONFLICT, decision.decisionType());
        verify(observabilityPort).trackConflictDetected(any(), any(), any());
        verify(eventRepository).save(any());
    }

    @Test
    public void testExpiredKey() {
        IdempotencyScope scope = new IdempotencyScope("tenant", "123", "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-1", "v1", "summary", "default");
        ClaimCommand command = new ClaimCommand(scope, key, fingerprint, "ref-1", "SYSTEM", "actor-1", "corr-1", "caus-1");

        IdempotencyRecord existingRecord = IdempotencyRecord.builder()
            .scope(scope)
            .key(key)
            .fingerprint(fingerprint)
            .state(IdempotencyState.COMPLETED)
            .replayWindowExpiresAt(Instant.now().minus(Duration.ofHours(1)))
            .build();

        when(recordRepository.findByScopeAndKey(scope, key)).thenReturn(Optional.of(existingRecord));

        IdempotencyDecision decision = claimService.claim(command);

        assertEquals(IdempotencyDecisionTypes.EXPIRED_KEY, decision.decisionType());
        verify(observabilityPort).trackExpiration(any(), any(), any());
    }

    @Test
    public void testIndeterminateOutcome() {
        IdempotencyScope scope = new IdempotencyScope("tenant", "123", "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-1", "v1", "summary", "default");
        ClaimCommand command = new ClaimCommand(scope, key, fingerprint, "ref-1", "SYSTEM", "actor-1", "corr-1", "caus-1");

        ReplayOutcome replayOutcome = ReplayOutcome.create(OutcomeType.INDETERMINATE, "UNKNOWN", "{}", 500, null);
        IdempotencyRecord existingRecord = IdempotencyRecord.builder()
            .scope(scope)
            .key(key)
            .fingerprint(fingerprint)
            .state(IdempotencyState.INDETERMINATE)
            .replayOutcome(replayOutcome)
            .build();

        when(recordRepository.findByScopeAndKey(scope, key)).thenReturn(Optional.of(existingRecord));

        IdempotencyDecision decision = claimService.claim(command);

        assertEquals(IdempotencyDecisionTypes.INDETERMINATE, decision.decisionType());
        assertEquals(replayOutcome, decision.replayOutcome());
        verify(observabilityPort).trackIndeterminateRecorded(any(), any(), any());
    }
}
