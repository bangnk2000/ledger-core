package com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject;

import java.util.Objects;

public record IdempotencyScope(
    String scopeType,
    String scopeValue,
    String operationKind,
    String policyProfile
) {
    public IdempotencyScope {
        Objects.requireNonNull(scopeType, "scopeType must not be null");
        Objects.requireNonNull(scopeValue, "scopeValue must not be null");
        Objects.requireNonNull(operationKind, "operationKind must not be null");
        Objects.requireNonNull(policyProfile, "policyProfile must not be null");
        if (scopeType.isBlank()) throw new IllegalArgumentException("scopeType must not be blank");
        if (scopeValue.isBlank()) throw new IllegalArgumentException("scopeValue must not be blank");
        if (operationKind.isBlank()) throw new IllegalArgumentException("operationKind must not be blank");
        if (policyProfile.isBlank()) throw new IllegalArgumentException("policyProfile must not be blank");
    }
}
