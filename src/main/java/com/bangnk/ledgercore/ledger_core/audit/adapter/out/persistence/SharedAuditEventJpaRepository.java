package com.bangnk.ledgercore.ledger_core.audit.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedAuditEventJpaRepository extends JpaRepository<AuditEventJpaEntity, UUID> {
    List<AuditEventJpaEntity> findByActiveRetentionUntilBefore(Instant asOf);
    List<AuditEventJpaEntity> findByRestrictedRetentionUntilBefore(Instant asOf);
}
