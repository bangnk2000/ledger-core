package com.bangnk.ledgercore.ledger_core.idempotency.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedIdempotencyRecordJpaRepository extends JpaRepository<IdempotencyRecordJpaEntity, UUID> {
    Optional<IdempotencyRecordJpaEntity> findByScopeTypeAndScopeValueAndOperationKindAndKeyValue(
        String scopeType, String scopeValue, String operationKind, String keyValue
    );
    Optional<IdempotencyRecordJpaEntity> findByClaimOwner(String claimOwner);
    List<IdempotencyRecordJpaEntity> findByRetentionStatusAndReplayWindowExpiresAtBefore(String retentionStatus, Instant now);
    List<IdempotencyRecordJpaEntity> findByTombstoneExpiresAtBefore(Instant now);
}
