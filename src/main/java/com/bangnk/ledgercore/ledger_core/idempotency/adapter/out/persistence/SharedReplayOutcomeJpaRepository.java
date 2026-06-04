package com.bangnk.ledgercore.ledger_core.idempotency.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedReplayOutcomeJpaRepository extends JpaRepository<ReplayOutcomeJpaEntity, UUID> {
}
