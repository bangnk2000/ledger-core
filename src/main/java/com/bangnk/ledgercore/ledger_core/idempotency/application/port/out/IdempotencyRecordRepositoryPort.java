package com.bangnk.ledgercore.ledger_core.idempotency.application.port.out;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.ClaimOwner;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyKey;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyScope;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRecordRepositoryPort {
    IdempotencyRecord save(IdempotencyRecord record);
    Optional<IdempotencyRecord> findById(UUID id);
    Optional<IdempotencyRecord> findByScopeAndKey(IdempotencyScope scope, IdempotencyKey key);
    Optional<IdempotencyRecord> findByClaimOwner(ClaimOwner claimOwner);
    List<IdempotencyRecord> findPurgeableRecords(Instant now);
    void delete(IdempotencyRecord record);
}
