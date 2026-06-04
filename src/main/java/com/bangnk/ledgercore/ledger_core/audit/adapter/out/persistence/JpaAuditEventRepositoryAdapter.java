package com.bangnk.ledgercore.ledger_core.audit.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.audit.application.port.out.AuditEventRepositoryPort;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.AuditEventType;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.IntegrityVerificationStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.PresenceStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ReferenceStatus;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEvent;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.ActorIdentity;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.AuditRetentionPolicyProfile;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IdempotencyReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.IntegrityProof;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.LedgerTransactionReference;
import com.bangnk.ledgercore.ledger_core.audit.domain.valueobject.TraceContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaAuditEventRepositoryAdapter implements AuditEventRepositoryPort {

    private final SharedAuditEventJpaRepository repository;
    private final ObjectMapper objectMapper;

    public JpaAuditEventRepositoryAdapter(SharedAuditEventJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public AuditEvent save(AuditEvent event) {
        repository.saveAndFlush(toEntity(event));
        return event;
    }

    @Override
    public Optional<AuditEvent> findById(UUID eventId) {
        return repository.findById(eventId).map(this::toDomain);
    }

    @Override
    public List<AuditEvent> findEligibleForRestrictedRetention(Instant asOf) {
        return repository.findByActiveRetentionUntilBefore(asOf).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditEvent> findEligibleForDisposition(Instant asOf) {
        return repository.findByRestrictedRetentionUntilBefore(asOf).stream().map(this::toDomain).toList();
    }

    private AuditEventJpaEntity toEntity(AuditEvent event) {
        AuditEventJpaEntity entity = new AuditEventJpaEntity();
        entity.setId(event.getId());
        entity.setEventType(event.getEventType().name());
        entity.setSchemaVersion(event.getSchemaVersion());
        entity.setModuleName(event.getModuleName());
        entity.setOccurredAt(event.getOccurredAt());
        entity.setCapturedAt(event.getCapturedAt());
        entity.setSubjectType(event.getSubjectType());
        entity.setSubjectId(event.getSubjectId());
        entity.setBusinessReference(event.getBusinessReference());
        entity.setStateFrom(event.getStateFrom());
        entity.setStateTo(event.getStateTo());
        entity.setSafeDetails(writeJson(event.getSafeDetails()));
        entity.setActorType(event.getActorIdentity().actorType().name());
        entity.setActorId(event.getActorIdentity().actorId());
        entity.setActorOrigin(event.getActorIdentity().origin());
        entity.setAuthorityContext(event.getActorIdentity().authorityContext());
        entity.setActorPresenceStatus(event.getActorIdentity().presenceStatus().name());
        entity.setCorrelationId(event.getTraceContext().correlationId());
        entity.setRequestId(event.getTraceContext().requestId());
        entity.setCausationId(event.getTraceContext().causationId());
        entity.setTracePresenceStatus(event.getTraceContext().presenceStatus().name());
        entity.setLedgerTransactionId(event.getLedgerTransactionReference().transactionId());
        entity.setLedgerPostingType(event.getLedgerTransactionReference().postingType());
        entity.setLedgerReferenceStatus(event.getLedgerTransactionReference().referenceStatus().name());
        entity.setIdempotencyKey(event.getIdempotencyReference().idempotencyKey());
        entity.setIdempotencyRecordId(event.getIdempotencyReference().idempotencyRecordId());
        entity.setIdempotencyScopeType(event.getIdempotencyReference().scopeType());
        entity.setIdempotencyScopeValue(event.getIdempotencyReference().scopeValue());
        entity.setIdempotencyReferenceStatus(event.getIdempotencyReference().referenceStatus().name());
        entity.setRetentionProfile(event.getRetentionPolicyProfile().profileName());
        entity.setActiveRetentionUntil(event.getRetentionPolicyProfile().activeRetentionUntil(event.getCapturedAt()));
        entity.setRestrictedRetentionUntil(event.getRetentionPolicyProfile().restrictedRetentionUntil(event.getCapturedAt()));
        entity.setFinalDispositionRule(event.getRetentionPolicyProfile().finalDispositionRule());
        entity.setRegulatoryClassification(event.getRetentionPolicyProfile().regulatoryClassification());
        entity.setIntegrityProofVersion(event.getIntegrityProof().proofVersion());
        entity.setIntegrityContentDigest(event.getIntegrityProof().contentDigest());
        entity.setIntegrityChainReference(event.getIntegrityProof().chainReference());
        entity.setIntegrityVerifiedAt(event.getIntegrityProof().verifiedAt());
        entity.setIntegrityVerificationStatus(event.getIntegrityProof().verificationStatus().name());
        return entity;
    }

    private AuditEvent toDomain(AuditEventJpaEntity entity) {
        return AuditEvent.builder()
            .id(entity.getId())
            .eventType(AuditEventType.valueOf(entity.getEventType()))
            .schemaVersion(entity.getSchemaVersion())
            .moduleName(entity.getModuleName())
            .occurredAt(entity.getOccurredAt())
            .capturedAt(entity.getCapturedAt())
            .subjectType(entity.getSubjectType())
            .subjectId(entity.getSubjectId())
            .businessReference(entity.getBusinessReference())
            .stateFrom(entity.getStateFrom())
            .stateTo(entity.getStateTo())
            .safeDetails(readJson(entity.getSafeDetails()))
            .actorIdentity(new ActorIdentity(
                ActorType.valueOf(entity.getActorType()),
                entity.getActorId(),
                entity.getActorOrigin(),
                entity.getAuthorityContext(),
                PresenceStatus.valueOf(entity.getActorPresenceStatus())
            ))
            .traceContext(new TraceContext(
                entity.getCorrelationId(),
                entity.getRequestId(),
                entity.getCausationId(),
                PresenceStatus.valueOf(entity.getTracePresenceStatus())
            ))
            .ledgerTransactionReference(new LedgerTransactionReference(
                entity.getLedgerTransactionId(),
                entity.getLedgerPostingType(),
                ReferenceStatus.valueOf(entity.getLedgerReferenceStatus())
            ))
            .idempotencyReference(new IdempotencyReference(
                entity.getIdempotencyKey(),
                entity.getIdempotencyRecordId(),
                entity.getIdempotencyScopeType(),
                entity.getIdempotencyScopeValue(),
                ReferenceStatus.valueOf(entity.getIdempotencyReferenceStatus())
            ))
            .retentionPolicyProfile(new AuditRetentionPolicyProfile(
                entity.getRetentionProfile(),
                derivedDuration(entity.getCapturedAt(), entity.getActiveRetentionUntil()),
                derivedDuration(entity.getActiveRetentionUntil(), entity.getRestrictedRetentionUntil()),
                entity.getFinalDispositionRule(),
                entity.getRegulatoryClassification()
            ))
            .integrityProof(new IntegrityProof(
                entity.getIntegrityProofVersion(),
                entity.getIntegrityContentDigest(),
                entity.getIntegrityChainReference(),
                entity.getIntegrityVerifiedAt(),
                IntegrityVerificationStatus.valueOf(entity.getIntegrityVerificationStatus())
            ))
            .build();
    }

    private Duration derivedDuration(Instant from, Instant to) {
        if (from == null || to == null || !to.isAfter(from)) {
            return Duration.ofDays(1);
        }
        return Duration.between(from, to);
    }

    private String writeJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize audit details", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJson(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, Map.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to deserialize audit details", e);
        }
    }
}
