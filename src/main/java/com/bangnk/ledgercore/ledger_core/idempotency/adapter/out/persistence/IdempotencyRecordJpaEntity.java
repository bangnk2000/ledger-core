package com.bangnk.ledgercore.ledger_core.idempotency.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecordJpaEntity {
    @Id
    private UUID id;
    @Column(name = "scope_type", nullable = false) private String scopeType;
    @Column(name = "scope_value", nullable = false) private String scopeValue;
    @Column(name = "operation_kind", nullable = false) private String operationKind;
    @Column(name = "policy_profile", nullable = false) private String policyProfile;
    @Column(name = "key_value", nullable = false) private String keyValue;
    @Column(name = "issued_at") private Instant issuedAt;
    @Column(name = "expires_at") private Instant expiresAt;
    @Column(name = "fingerprint_value", nullable = false) private String fingerprintValue;
    @Column(name = "fingerprint_version", nullable = false) private String fingerprintVersion;
    @Column(name = "material_fields_summary") private String materialFieldsSummary;
    @Column(name = "canonicalization_profile") private String canonicalizationProfile;
    @Column(name = "state", nullable = false) private String state;
    @Column(name = "claim_owner") private String claimOwner;
    @Column(name = "claim_acquired_at") private Instant claimAcquiredAt;
    @Column(name = "last_transition_at", nullable = false) private Instant lastTransitionAt;
    @Column(name = "first_seen_at", nullable = false) private Instant firstSeenAt;
    @Column(name = "last_seen_at", nullable = false) private Instant lastSeenAt;
    @Column(name = "replay_window_expires_at") private Instant replayWindowExpiresAt;
    @Column(name = "tombstone_expires_at") private Instant tombstoneExpiresAt;
    @Column(name = "retention_status", nullable = false) private String retentionStatus;
    @Column(name = "business_reference") private String businessReference;
    @Column(name = "correlation_id") private String correlationId;
    @Column(name = "causation_id") private String causationId;
    @Column(name = "attempt_count", nullable = false) private int attemptCount;

    public IdempotencyRecordJpaEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String v) { this.scopeType = v; }
    public String getScopeValue() { return scopeValue; }
    public void setScopeValue(String v) { this.scopeValue = v; }
    public String getOperationKind() { return operationKind; }
    public void setOperationKind(String v) { this.operationKind = v; }
    public String getPolicyProfile() { return policyProfile; }
    public void setPolicyProfile(String v) { this.policyProfile = v; }
    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String v) { this.keyValue = v; }
    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant v) { this.issuedAt = v; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant v) { this.expiresAt = v; }
    public String getFingerprintValue() { return fingerprintValue; }
    public void setFingerprintValue(String v) { this.fingerprintValue = v; }
    public String getFingerprintVersion() { return fingerprintVersion; }
    public void setFingerprintVersion(String v) { this.fingerprintVersion = v; }
    public String getMaterialFieldsSummary() { return materialFieldsSummary; }
    public void setMaterialFieldsSummary(String v) { this.materialFieldsSummary = v; }
    public String getCanonicalizationProfile() { return canonicalizationProfile; }
    public void setCanonicalizationProfile(String v) { this.canonicalizationProfile = v; }
    public String getState() { return state; }
    public void setState(String v) { this.state = v; }
    public String getClaimOwner() { return claimOwner; }
    public void setClaimOwner(String v) { this.claimOwner = v; }
    public Instant getClaimAcquiredAt() { return claimAcquiredAt; }
    public void setClaimAcquiredAt(Instant v) { this.claimAcquiredAt = v; }
    public Instant getLastTransitionAt() { return lastTransitionAt; }
    public void setLastTransitionAt(Instant v) { this.lastTransitionAt = v; }
    public Instant getFirstSeenAt() { return firstSeenAt; }
    public void setFirstSeenAt(Instant v) { this.firstSeenAt = v; }
    public Instant getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Instant v) { this.lastSeenAt = v; }
    public Instant getReplayWindowExpiresAt() { return replayWindowExpiresAt; }
    public void setReplayWindowExpiresAt(Instant v) { this.replayWindowExpiresAt = v; }
    public Instant getTombstoneExpiresAt() { return tombstoneExpiresAt; }
    public void setTombstoneExpiresAt(Instant v) { this.tombstoneExpiresAt = v; }
    public String getRetentionStatus() { return retentionStatus; }
    public void setRetentionStatus(String v) { this.retentionStatus = v; }
    public String getBusinessReference() { return businessReference; }
    public void setBusinessReference(String v) { this.businessReference = v; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String v) { this.correlationId = v; }
    public String getCausationId() { return causationId; }
    public void setCausationId(String v) { this.causationId = v; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int v) { this.attemptCount = v; }
}
