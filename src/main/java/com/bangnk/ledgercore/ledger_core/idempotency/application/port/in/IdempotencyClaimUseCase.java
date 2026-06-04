package com.bangnk.ledgercore.ledger_core.idempotency.application.port.in;

import com.bangnk.ledgercore.ledger_core.idempotency.application.IdempotencyDecision;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyKey;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyScope;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.RequestFingerprint;

public interface IdempotencyClaimUseCase {
    IdempotencyDecision claim(ClaimCommand command);

    record ClaimCommand(
        IdempotencyScope scope,
        IdempotencyKey key,
        RequestFingerprint fingerprint,
        String businessReference,
        String actorType,
        String actorId,
        String correlationId,
        String causationId
    ) {}
}
