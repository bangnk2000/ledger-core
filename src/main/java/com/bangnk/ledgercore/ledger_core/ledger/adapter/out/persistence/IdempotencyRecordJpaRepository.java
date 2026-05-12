package com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerJpaEntities.IdempotencyRecordJpaEntity;
import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerJpaEntities.IdempotencyRecordKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordJpaRepository extends JpaRepository<IdempotencyRecordJpaEntity, IdempotencyRecordKey> {
}
