package com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record ClaimOwner(String ownerToken) {
    public ClaimOwner {
        Objects.requireNonNull(ownerToken, "ownerToken must not be null");
        if (ownerToken.isBlank()) throw new IllegalArgumentException("ownerToken must not be blank");
    }

    public static ClaimOwner generate() {
        return new ClaimOwner(UUID.randomUUID().toString());
    }
}
