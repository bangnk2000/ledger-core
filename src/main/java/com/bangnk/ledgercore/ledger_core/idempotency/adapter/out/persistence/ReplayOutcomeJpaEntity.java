package com.bangnk.ledgercore.ledger_core.idempotency.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_replay_outcomes")
public class ReplayOutcomeJpaEntity {
    @Id
    @Column(name = "record_id")
    private UUID recordId;
    @Column(name = "outcome_type", nullable = false) private String outcomeType;
    @Column(name = "response_code") private String responseCode;
    @Column(name = "response_payload") private String responsePayload;
    @Column(name = "http_status_hint") private Integer httpStatusHint;
    @Column(name = "business_result_reference") private String businessResultReference;
    @Column(name = "finalized_at", nullable = false) private Instant finalizedAt;

    public ReplayOutcomeJpaEntity() {}

    public UUID getRecordId() { return recordId; }
    public void setRecordId(UUID v) { this.recordId = v; }
    public String getOutcomeType() { return outcomeType; }
    public void setOutcomeType(String v) { this.outcomeType = v; }
    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String v) { this.responseCode = v; }
    public String getResponsePayload() { return responsePayload; }
    public void setResponsePayload(String v) { this.responsePayload = v; }
    public Integer getHttpStatusHint() { return httpStatusHint; }
    public void setHttpStatusHint(Integer v) { this.httpStatusHint = v; }
    public String getBusinessResultReference() { return businessResultReference; }
    public void setBusinessResultReference(String v) { this.businessResultReference = v; }
    public Instant getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(Instant v) { this.finalizedAt = v; }
}
