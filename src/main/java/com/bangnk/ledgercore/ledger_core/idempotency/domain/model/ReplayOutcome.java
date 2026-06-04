package com.bangnk.ledgercore.ledger_core.idempotency.domain.model;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.OutcomeType;
import java.time.Instant;
import java.util.Objects;

public record ReplayOutcome(
    OutcomeType outcomeType,
    String responseCode,
    String responsePayload,
    Integer httpStatusHint,
    String businessResultReference,
    Instant finalizedAt
) {
    public ReplayOutcome {
        Objects.requireNonNull(outcomeType, "outcomeType must not be null");
        Objects.requireNonNull(finalizedAt, "finalizedAt must not be null");
    }

    public static ReplayOutcome create(
        OutcomeType outcomeType,
        String responseCode,
        String responsePayload,
        Integer httpStatusHint,
        String businessResultReference
    ) {
        return new ReplayOutcome(
            outcomeType,
            responseCode,
            responsePayload,
            httpStatusHint,
            businessResultReference,
            Instant.now()
        );
    }
}
