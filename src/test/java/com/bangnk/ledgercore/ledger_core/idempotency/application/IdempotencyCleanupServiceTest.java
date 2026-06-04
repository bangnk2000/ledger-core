package com.bangnk.ledgercore.ledger_core.idempotency.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyObservabilityPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyRecordRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.LifecycleEventRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.IdempotencyState;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.RetentionStatus;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyKey;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyScope;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.RequestFingerprint;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdempotencyCleanupServiceTest {

    private IdempotencyRecordRepositoryPort recordRepository;
    private LifecycleEventRepositoryPort eventRepository;
    private IdempotencyObservabilityPort observabilityPort;
    private IdempotencyCleanupService service;

    @BeforeEach
    void setUp() {
        recordRepository = mock(IdempotencyRecordRepositoryPort.class);
        eventRepository = mock(LifecycleEventRepositoryPort.class);
        observabilityPort = mock(IdempotencyObservabilityPort.class);
        service = new IdempotencyCleanupService(recordRepository, eventRepository, observabilityPort);
    }

    @Test
    void tombstonesRecordsWhoseReplayWindowHasElapsed() {
        Instant now = Instant.parse("2026-06-04T00:00:00Z");
        IdempotencyRecord replayExpired = terminalRecord(
            Instant.parse("2026-06-03T00:00:00Z"),
            Instant.parse("2026-06-10T00:00:00Z")
        );
        when(recordRepository.findReplayWindowExpiredRecords(now)).thenReturn(List.of(replayExpired));
        when(recordRepository.findPurgeableRecords(now)).thenReturn(List.of());

        int cleaned = service.cleanupExpiredRecords(now);

        assertEquals(0, cleaned);
        assertEquals(RetentionStatus.TOMBSTONED, replayExpired.getRetentionStatus());
        verify(recordRepository).save(replayExpired);
        verify(eventRepository).save(any());
        verify(observabilityPort).trackCleanup(0);
    }

    @Test
    void deletesRecordsWhoseTombstoneWindowHasElapsed() {
        Instant now = Instant.parse("2026-06-12T00:00:00Z");
        IdempotencyRecord purgeable = terminalRecord(
            Instant.parse("2026-06-03T00:00:00Z"),
            Instant.parse("2026-06-04T00:00:00Z")
        );
        purgeable.setRetentionStatus(RetentionStatus.TOMBSTONED);
        when(recordRepository.findReplayWindowExpiredRecords(now)).thenReturn(List.of());
        when(recordRepository.findPurgeableRecords(now)).thenReturn(List.of(purgeable));

        int cleaned = service.cleanupExpiredRecords(now);

        assertEquals(1, cleaned);
        verify(recordRepository).delete(purgeable);
        verify(eventRepository).save(any());
        verify(observabilityPort).trackCleanup(1);
    }

    private static IdempotencyRecord terminalRecord(Instant replayWindowExpiresAt, Instant tombstoneExpiresAt) {
        return IdempotencyRecord.builder()
            .scope(new IdempotencyScope("tenant", "scope-1", "reserve", "DEFAULT"))
            .key(new IdempotencyKey("key-1"))
            .fingerprint(new RequestFingerprint("hash-1", "v1", "amount=100", "profile"))
            .state(IdempotencyState.COMPLETED)
            .replayWindowExpiresAt(replayWindowExpiresAt)
            .tombstoneExpiresAt(tombstoneExpiresAt)
            .retentionStatus(RetentionStatus.REPLAYABLE)
            .build();
    }
}
