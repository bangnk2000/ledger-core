package com.bangnk.ledgercore.ledger_core.idempotency.application.port.in;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.OutcomeType;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.ClaimOwner;
import java.time.Instant;
import java.util.UUID;

public interface IdempotencyFinalizeUseCase {
    void finalizeClaim(FinalizeCommand command);

    record FinalizeCommand(
        UUID recordId,
        ClaimOwner claimOwner,
        OutcomeType outcomeType,
        String responseCode,
        String responsePayload,
        String businessResultReference,
        Instant finalizedAt,
        String indeterminateReason
    ) {}
}
