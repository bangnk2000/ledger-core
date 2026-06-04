package com.bangnk.ledgercore.ledger_core.audit.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedPublicationAttemptJpaRepository extends JpaRepository<PublicationAttemptJpaEntity, UUID> {
    List<PublicationAttemptJpaEntity> findByResultAndNextRetryAtLessThanEqual(String result, Instant asOf);
}
