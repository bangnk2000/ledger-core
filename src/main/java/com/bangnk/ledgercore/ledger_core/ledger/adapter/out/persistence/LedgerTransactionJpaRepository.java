package com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerJpaEntities.LedgerTransactionJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerTransactionJpaRepository extends JpaRepository<LedgerTransactionJpaEntity, UUID> {
}
