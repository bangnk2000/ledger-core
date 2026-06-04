package com.bangnk.ledgercore.ledger_core.idempotency.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyRecordRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.IdempotencyState;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.RetentionStatus;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.ClaimOwner;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyKey;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyScope;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.RequestFingerprint;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
public class JpaIdempotencyRecordRepositoryAdapter implements IdempotencyRecordRepositoryPort {

    private final SharedIdempotencyRecordJpaRepository repository;
    private final SharedReplayOutcomeJpaRepository outcomeRepository;
    private final JdbcTemplate jdbcTemplate;

    public JpaIdempotencyRecordRepositoryAdapter(
        SharedIdempotencyRecordJpaRepository repository,
        SharedReplayOutcomeJpaRepository outcomeRepository,
        JdbcTemplate jdbcTemplate
    ) {
        this.repository = repository;
        this.outcomeRepository = outcomeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean createIfAbsent(IdempotencyRecord record) {
        int inserted = jdbcTemplate.update(
            """
            INSERT INTO idempotency_records (
                id, scope_type, scope_value, operation_kind, policy_profile, key_value, issued_at, expires_at,
                fingerprint_value, fingerprint_version, material_fields_summary, canonicalization_profile,
                state, claim_owner, claim_acquired_at, last_transition_at, first_seen_at, last_seen_at,
                replay_window_expires_at, tombstone_expires_at, retention_status, business_reference,
                correlation_id, causation_id, attempt_count
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (scope_type, scope_value, operation_kind, key_value) DO NOTHING
            """,
            record.getId(),
            record.getScope().scopeType(),
            record.getScope().scopeValue(),
            record.getScope().operationKind(),
            record.getScope().policyProfile(),
            record.getKey().keyValue(),
            toTimestamp(record.getKey().issuedAt()),
            toTimestamp(record.getKey().expiresAt()),
            record.getFingerprint().fingerprintValue(),
            record.getFingerprint().fingerprintVersion(),
            record.getFingerprint().materialFieldsSummary(),
            record.getFingerprint().canonicalizationProfile(),
            record.getState().name(),
            record.getClaimOwner() != null ? record.getClaimOwner().ownerToken() : null,
            toTimestamp(record.getClaimAcquiredAt()),
            toTimestamp(record.getLastTransitionAt()),
            toTimestamp(record.getFirstSeenAt()),
            toTimestamp(record.getLastSeenAt()),
            toTimestamp(record.getReplayWindowExpiresAt()),
            toTimestamp(record.getTombstoneExpiresAt()),
            record.getRetentionStatus().name(),
            record.getBusinessReference(),
            record.getCorrelationId(),
            record.getCausationId(),
            record.getAttemptCount()
        );
        return inserted == 1;
    }

    @Override
    public IdempotencyRecord save(IdempotencyRecord record) {
        repository.saveAndFlush(toEntity(record));
        return record;
    }

    @Override
    public Optional<IdempotencyRecord> findById(UUID id) {
        return repository.findById(id).map(this::toDomainWithOutcome);
    }

    @Override
    public Optional<IdempotencyRecord> findByScopeAndKey(IdempotencyScope scope, IdempotencyKey key) {
        return repository.findByScopeTypeAndScopeValueAndOperationKindAndKeyValue(
            scope.scopeType(), scope.scopeValue(), scope.operationKind(), key.keyValue()
        ).map(this::toDomainWithOutcome);
    }

    @Override
    public Optional<IdempotencyRecord> findByClaimOwner(ClaimOwner claimOwner) {
        return repository.findByClaimOwner(claimOwner.ownerToken()).map(this::toDomainWithOutcome);
    }

    @Override
    public List<IdempotencyRecord> findReplayWindowExpiredRecords(Instant now) {
        return repository.findByRetentionStatusAndReplayWindowExpiresAtBefore(
            RetentionStatus.REPLAYABLE.name(),
            now
        ).stream().map(this::toDomainWithOutcome).toList();
    }

    @Override
    public List<IdempotencyRecord> findPurgeableRecords(Instant now) {
        return repository.findByTombstoneExpiresAtBefore(now).stream()
            .map(this::toDomainWithOutcome)
            .filter(record -> record.getRetentionStatus() == RetentionStatus.TOMBSTONED
                || record.getRetentionStatus() == RetentionStatus.PURGE_ELIGIBLE)
            .toList();
    }

    @Override
    public void delete(IdempotencyRecord record) {
        repository.deleteById(record.getId());
    }

    private IdempotencyRecordJpaEntity toEntity(IdempotencyRecord record) {
        IdempotencyRecordJpaEntity entity = new IdempotencyRecordJpaEntity();
        entity.setId(record.getId());
        entity.setScopeType(record.getScope().scopeType());
        entity.setScopeValue(record.getScope().scopeValue());
        entity.setOperationKind(record.getScope().operationKind());
        entity.setPolicyProfile(record.getScope().policyProfile());
        entity.setKeyValue(record.getKey().keyValue());
        entity.setIssuedAt(record.getKey().issuedAt());
        entity.setExpiresAt(record.getKey().expiresAt());
        entity.setFingerprintValue(record.getFingerprint().fingerprintValue());
        entity.setFingerprintVersion(record.getFingerprint().fingerprintVersion());
        entity.setMaterialFieldsSummary(record.getFingerprint().materialFieldsSummary());
        entity.setCanonicalizationProfile(record.getFingerprint().canonicalizationProfile());
        entity.setState(record.getState().name());
        entity.setClaimOwner(record.getClaimOwner() != null ? record.getClaimOwner().ownerToken() : null);
        entity.setClaimAcquiredAt(record.getClaimAcquiredAt());
        entity.setLastTransitionAt(record.getLastTransitionAt());
        entity.setFirstSeenAt(record.getFirstSeenAt());
        entity.setLastSeenAt(record.getLastSeenAt());
        entity.setReplayWindowExpiresAt(record.getReplayWindowExpiresAt());
        entity.setTombstoneExpiresAt(record.getTombstoneExpiresAt());
        entity.setRetentionStatus(record.getRetentionStatus().name());
        entity.setBusinessReference(record.getBusinessReference());
        entity.setCorrelationId(record.getCorrelationId());
        entity.setCausationId(record.getCausationId());
        entity.setAttemptCount(record.getAttemptCount());
        return entity;
    }

    private IdempotencyRecord toDomain(IdempotencyRecordJpaEntity entity) {
        IdempotencyScope scope = new IdempotencyScope(
            entity.getScopeType(), entity.getScopeValue(), entity.getOperationKind(), entity.getPolicyProfile()
        );
        IdempotencyKey key = new IdempotencyKey(
            entity.getKeyValue(), entity.getIssuedAt(), entity.getExpiresAt(), entity.getTombstoneExpiresAt()
        );
        RequestFingerprint fingerprint = new RequestFingerprint(
            entity.getFingerprintValue(), entity.getFingerprintVersion(), entity.getMaterialFieldsSummary(), entity.getCanonicalizationProfile()
        );
        
        return IdempotencyRecord.builder()
            .id(entity.getId())
            .scope(scope)
            .key(key)
            .fingerprint(fingerprint)
            .state(IdempotencyState.valueOf(entity.getState()))
            .claimOwner(entity.getClaimOwner() != null ? new ClaimOwner(entity.getClaimOwner()) : null)
            .claimAcquiredAt(entity.getClaimAcquiredAt())
            .lastTransitionAt(entity.getLastTransitionAt())
            .firstSeenAt(entity.getFirstSeenAt())
            .lastSeenAt(entity.getLastSeenAt())
            .replayWindowExpiresAt(entity.getReplayWindowExpiresAt())
            .tombstoneExpiresAt(entity.getTombstoneExpiresAt())
            .retentionStatus(RetentionStatus.valueOf(entity.getRetentionStatus()))
            .businessReference(entity.getBusinessReference())
            .correlationId(entity.getCorrelationId())
            .causationId(entity.getCausationId())
            .attemptCount(entity.getAttemptCount())
            .build();
    }

    private IdempotencyRecord toDomainWithOutcome(IdempotencyRecordJpaEntity entity) {
        IdempotencyRecord record = toDomain(entity);
        outcomeRepository.findById(entity.getId()).ifPresent(outcomeEntity ->
            record.setReplayOutcome(
                new com.bangnk.ledgercore.ledger_core.idempotency.domain.model.ReplayOutcome(
                    com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.OutcomeType.valueOf(
                        outcomeEntity.getOutcomeType()
                    ),
                    outcomeEntity.getResponseCode(),
                    outcomeEntity.getResponsePayload(),
                    outcomeEntity.getHttpStatusHint(),
                    outcomeEntity.getBusinessResultReference(),
                    outcomeEntity.getFinalizedAt()
                )
            )
        );
        return record;
    }

    private Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}
