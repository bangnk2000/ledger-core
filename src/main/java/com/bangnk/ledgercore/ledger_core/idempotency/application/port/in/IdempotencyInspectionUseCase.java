package com.bangnk.ledgercore.ledger_core.idempotency.application.port.in;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyKey;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyScope;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyInspectionUseCase {
    Optional<IdempotencyRecord> inspectRecord(UUID recordId);
    Optional<IdempotencyRecord> inspectRecord(IdempotencyScope scope, IdempotencyKey key);
}
