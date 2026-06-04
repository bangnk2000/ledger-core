package com.bangnk.ledgercore.ledger_core.idempotency.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.IdempotencyState;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.OutcomeType;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.ReplayOutcome;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.ClaimOwner;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyKey;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyScope;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.RequestFingerprint;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class IdempotencyRecordTest {

    @Test
    public void testFirstExecutionClaim() {
        IdempotencyScope scope = new IdempotencyScope("tenant", "123", "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-1", "v1", "summary", "default");
        
        IdempotencyRecord record = IdempotencyRecord.builder()
            .id(UUID.randomUUID())
            .scope(scope)
            .key(key)
            .fingerprint(fingerprint)
            .state(IdempotencyState.RECEIVED)
            .build();
            
        ClaimOwner owner = ClaimOwner.generate();
        record.claim(owner, Instant.now());
        
        assertEquals(IdempotencyState.CLAIMED, record.getState());
        assertEquals(owner, record.getClaimOwner());
    }
    
    @Test
    public void testFinalizeSuccess() {
        IdempotencyScope scope = new IdempotencyScope("tenant", "123", "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-1", "v1", "summary", "default");
        ClaimOwner owner = ClaimOwner.generate();
        
        IdempotencyRecord record = IdempotencyRecord.builder()
            .id(UUID.randomUUID())
            .scope(scope)
            .key(key)
            .fingerprint(fingerprint)
            .state(IdempotencyState.CLAIMED)
            .claimOwner(owner)
            .build();
            
        ReplayOutcome outcome = ReplayOutcome.create(OutcomeType.REPLAY, "SUCCESS", "{}", 200, "tx-123");
        record.finalizeRecord(owner, outcome);
        
        assertEquals(IdempotencyState.COMPLETED, record.getState());
        assertEquals(outcome, record.getReplayOutcome());
    }
    
    @Test
    public void testFinalizeOwnerMismatch() {
        IdempotencyScope scope = new IdempotencyScope("tenant", "123", "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-1", "v1", "summary", "default");
        ClaimOwner owner1 = ClaimOwner.generate();
        ClaimOwner owner2 = ClaimOwner.generate();
        
        IdempotencyRecord record = IdempotencyRecord.builder()
            .id(UUID.randomUUID())
            .scope(scope)
            .key(key)
            .fingerprint(fingerprint)
            .state(IdempotencyState.CLAIMED)
            .claimOwner(owner1)
            .build();
            
        ReplayOutcome outcome = ReplayOutcome.create(OutcomeType.REPLAY, "SUCCESS", "{}", 200, "tx-123");
        
        assertThrows(IllegalArgumentException.class, () -> record.finalizeRecord(owner2, outcome));
    }

    @Test
    public void testCannotClaimTwice() {
        IdempotencyScope scope = new IdempotencyScope("tenant", "123", "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-1", "v1", "summary", "default");
        IdempotencyRecord record = IdempotencyRecord.builder()
            .scope(scope).key(key).fingerprint(fingerprint)
            .state(IdempotencyState.CLAIMED)
            .build();
            
        assertThrows(IllegalStateException.class, () -> record.claim(ClaimOwner.generate(), Instant.now()));
    }

    @Test
    public void testFinalizeIndeterminate() {
        IdempotencyScope scope = new IdempotencyScope("tenant", "123", "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-1", "v1", "summary", "default");
        ClaimOwner owner = ClaimOwner.generate();
        IdempotencyRecord record = IdempotencyRecord.builder()
            .scope(scope).key(key).fingerprint(fingerprint)
            .state(IdempotencyState.CLAIMED).claimOwner(owner)
            .build();
            
        ReplayOutcome outcome = ReplayOutcome.create(OutcomeType.INDETERMINATE, "UNKNOWN", "{}", 500, null);
        record.finalizeRecord(owner, outcome);
        
        assertEquals(IdempotencyState.INDETERMINATE, record.getState());
    }

    @Test
    public void testFinalizeRejected() {
        IdempotencyScope scope = new IdempotencyScope("tenant", "123", "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-1", "v1", "summary", "default");
        ClaimOwner owner = ClaimOwner.generate();
        IdempotencyRecord record = IdempotencyRecord.builder()
            .scope(scope).key(key).fingerprint(fingerprint)
            .state(IdempotencyState.CLAIMED).claimOwner(owner)
            .build();
            
        ReplayOutcome outcome = ReplayOutcome.create(OutcomeType.CONFLICT, "CONFLICT", "{}", 409, null);
        record.finalizeRecord(owner, outcome);
        
        assertEquals(IdempotencyState.REJECTED, record.getState());
    }
}
