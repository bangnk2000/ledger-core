package com.bangnk.ledgercore.ledger_core.idempotency.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.RequestFingerprint;
import org.junit.jupiter.api.Test;

class RequestFingerprintTest {

    @Test
    void matchesWhenBusinessIntentFingerprintIsTheSame() {
        RequestFingerprint original = new RequestFingerprint("hash-1", "v1", "amount=100", "ledger-posting-v1");
        RequestFingerprint retry = new RequestFingerprint("hash-1", "v1", "amount=100", "ledger-posting-v1");

        assertTrue(original.matches(retry));
        assertFalse(original.conflictsWith(retry));
    }

    @Test
    void conflictsWhenFingerprintValueChangesForSameKeyReuse() {
        RequestFingerprint original = new RequestFingerprint("hash-1", "v1", "amount=100", "ledger-posting-v1");
        RequestFingerprint conflicting = new RequestFingerprint("hash-2", "v1", "amount=250", "ledger-posting-v1");

        assertFalse(original.matches(conflicting));
        assertTrue(original.conflictsWith(conflicting));
    }
}
