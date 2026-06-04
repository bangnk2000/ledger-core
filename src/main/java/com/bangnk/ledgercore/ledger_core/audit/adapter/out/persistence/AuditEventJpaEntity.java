package com.bangnk.ledgercore.ledger_core.audit.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEventJpaEntity {
    @Id
    private UUID id;
    @Column(name = "event_type", nullable = false) private String eventType;
    @Column(name = "schema_version", nullable = false) private String schemaVersion;
    @Column(name = "module_name", nullable = false) private String moduleName;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
    @Column(name = "captured_at", nullable = false) private Instant capturedAt;
    @Column(name = "subject_type", nullable = false) private String subjectType;
    @Column(name = "subject_id", nullable = false) private String subjectId;
    @Column(name = "business_reference") private String businessReference;
    @Column(name = "state_from") private String stateFrom;
    @Column(name = "state_to") private String stateTo;
    @Column(name = "safe_details") private String safeDetails;
    @Column(name = "actor_type", nullable = false) private String actorType;
    @Column(name = "actor_id") private String actorId;
    @Column(name = "actor_origin") private String actorOrigin;
    @Column(name = "authority_context") private String authorityContext;
    @Column(name = "actor_presence_status", nullable = false) private String actorPresenceStatus;
    @Column(name = "correlation_id") private String correlationId;
    @Column(name = "request_id") private String requestId;
    @Column(name = "causation_id") private String causationId;
    @Column(name = "trace_presence_status", nullable = false) private String tracePresenceStatus;
    @Column(name = "ledger_transaction_id") private String ledgerTransactionId;
    @Column(name = "ledger_posting_type") private String ledgerPostingType;
    @Column(name = "ledger_reference_status", nullable = false) private String ledgerReferenceStatus;
    @Column(name = "idempotency_key") private String idempotencyKey;
    @Column(name = "idempotency_record_id") private String idempotencyRecordId;
    @Column(name = "idempotency_scope_type") private String idempotencyScopeType;
    @Column(name = "idempotency_scope_value") private String idempotencyScopeValue;
    @Column(name = "idempotency_reference_status", nullable = false) private String idempotencyReferenceStatus;
    @Column(name = "retention_profile", nullable = false) private String retentionProfile;
    @Column(name = "active_retention_until") private Instant activeRetentionUntil;
    @Column(name = "restricted_retention_until") private Instant restrictedRetentionUntil;
    @Column(name = "final_disposition_rule") private String finalDispositionRule;
    @Column(name = "regulatory_classification") private String regulatoryClassification;
    @Column(name = "integrity_proof_version", nullable = false) private String integrityProofVersion;
    @Column(name = "integrity_content_digest", nullable = false) private String integrityContentDigest;
    @Column(name = "integrity_chain_reference") private String integrityChainReference;
    @Column(name = "integrity_verified_at") private Instant integrityVerifiedAt;
    @Column(name = "integrity_verification_status", nullable = false) private String integrityVerificationStatus;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public Instant getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Instant capturedAt) { this.capturedAt = capturedAt; }
    public String getSubjectType() { return subjectType; }
    public void setSubjectType(String subjectType) { this.subjectType = subjectType; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getBusinessReference() { return businessReference; }
    public void setBusinessReference(String businessReference) { this.businessReference = businessReference; }
    public String getStateFrom() { return stateFrom; }
    public void setStateFrom(String stateFrom) { this.stateFrom = stateFrom; }
    public String getStateTo() { return stateTo; }
    public void setStateTo(String stateTo) { this.stateTo = stateTo; }
    public String getSafeDetails() { return safeDetails; }
    public void setSafeDetails(String safeDetails) { this.safeDetails = safeDetails; }
    public String getActorType() { return actorType; }
    public void setActorType(String actorType) { this.actorType = actorType; }
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
    public String getActorOrigin() { return actorOrigin; }
    public void setActorOrigin(String actorOrigin) { this.actorOrigin = actorOrigin; }
    public String getAuthorityContext() { return authorityContext; }
    public void setAuthorityContext(String authorityContext) { this.authorityContext = authorityContext; }
    public String getActorPresenceStatus() { return actorPresenceStatus; }
    public void setActorPresenceStatus(String actorPresenceStatus) { this.actorPresenceStatus = actorPresenceStatus; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getCausationId() { return causationId; }
    public void setCausationId(String causationId) { this.causationId = causationId; }
    public String getTracePresenceStatus() { return tracePresenceStatus; }
    public void setTracePresenceStatus(String tracePresenceStatus) { this.tracePresenceStatus = tracePresenceStatus; }
    public String getLedgerTransactionId() { return ledgerTransactionId; }
    public void setLedgerTransactionId(String ledgerTransactionId) { this.ledgerTransactionId = ledgerTransactionId; }
    public String getLedgerPostingType() { return ledgerPostingType; }
    public void setLedgerPostingType(String ledgerPostingType) { this.ledgerPostingType = ledgerPostingType; }
    public String getLedgerReferenceStatus() { return ledgerReferenceStatus; }
    public void setLedgerReferenceStatus(String ledgerReferenceStatus) { this.ledgerReferenceStatus = ledgerReferenceStatus; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getIdempotencyRecordId() { return idempotencyRecordId; }
    public void setIdempotencyRecordId(String idempotencyRecordId) { this.idempotencyRecordId = idempotencyRecordId; }
    public String getIdempotencyScopeType() { return idempotencyScopeType; }
    public void setIdempotencyScopeType(String idempotencyScopeType) { this.idempotencyScopeType = idempotencyScopeType; }
    public String getIdempotencyScopeValue() { return idempotencyScopeValue; }
    public void setIdempotencyScopeValue(String idempotencyScopeValue) { this.idempotencyScopeValue = idempotencyScopeValue; }
    public String getIdempotencyReferenceStatus() { return idempotencyReferenceStatus; }
    public void setIdempotencyReferenceStatus(String idempotencyReferenceStatus) { this.idempotencyReferenceStatus = idempotencyReferenceStatus; }
    public String getRetentionProfile() { return retentionProfile; }
    public void setRetentionProfile(String retentionProfile) { this.retentionProfile = retentionProfile; }
    public Instant getActiveRetentionUntil() { return activeRetentionUntil; }
    public void setActiveRetentionUntil(Instant activeRetentionUntil) { this.activeRetentionUntil = activeRetentionUntil; }
    public Instant getRestrictedRetentionUntil() { return restrictedRetentionUntil; }
    public void setRestrictedRetentionUntil(Instant restrictedRetentionUntil) { this.restrictedRetentionUntil = restrictedRetentionUntil; }
    public String getFinalDispositionRule() { return finalDispositionRule; }
    public void setFinalDispositionRule(String finalDispositionRule) { this.finalDispositionRule = finalDispositionRule; }
    public String getRegulatoryClassification() { return regulatoryClassification; }
    public void setRegulatoryClassification(String regulatoryClassification) { this.regulatoryClassification = regulatoryClassification; }
    public String getIntegrityProofVersion() { return integrityProofVersion; }
    public void setIntegrityProofVersion(String integrityProofVersion) { this.integrityProofVersion = integrityProofVersion; }
    public String getIntegrityContentDigest() { return integrityContentDigest; }
    public void setIntegrityContentDigest(String integrityContentDigest) { this.integrityContentDigest = integrityContentDigest; }
    public String getIntegrityChainReference() { return integrityChainReference; }
    public void setIntegrityChainReference(String integrityChainReference) { this.integrityChainReference = integrityChainReference; }
    public Instant getIntegrityVerifiedAt() { return integrityVerifiedAt; }
    public void setIntegrityVerifiedAt(Instant integrityVerifiedAt) { this.integrityVerifiedAt = integrityVerifiedAt; }
    public String getIntegrityVerificationStatus() { return integrityVerificationStatus; }
    public void setIntegrityVerificationStatus(String integrityVerificationStatus) { this.integrityVerificationStatus = integrityVerificationStatus; }
}
