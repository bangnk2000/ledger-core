package com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject;

import java.time.Instant;
import java.util.Objects;

public record IdempotencyKey(
    String keyValue,
    Instant issuedAt,
    Instant expiresAt,
    Instant tombstoneExpiresAt
) {
    public IdempotencyKey {
        Objects.requireNonNull(keyValue, "keyValue must not be null");
        if (keyValue.isBlank()) throw new IllegalArgumentException("keyValue must not be blank");
    }

    public IdempotencyKey(String keyValue) {
        this(keyValue, null, null, null);
    }
    
    public IdempotencyKey withExpiration(Instant expiresAt, Instant tombstoneExpiresAt) {
        return new IdempotencyKey(this.keyValue, this.issuedAt, expiresAt, tombstoneExpiresAt);
    }
}
