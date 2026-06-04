package com.bangnk.ledgercore.ledger_core.audit.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_publication_attempts")
public class PublicationAttemptJpaEntity {
    @Id
    private UUID id;
    @Column(name = "event_id", nullable = false) private UUID eventId;
    @Column(name = "destination_type", nullable = false) private String destinationType;
    @Column(name = "attempted_at", nullable = false) private Instant attemptedAt;
    @Column(name = "result", nullable = false) private String result;
    @Column(name = "failure_reason") private String failureReason;
    @Column(name = "next_retry_at") private Instant nextRetryAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public String getDestinationType() { return destinationType; }
    public void setDestinationType(String destinationType) { this.destinationType = destinationType; }
    public Instant getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(Instant attemptedAt) { this.attemptedAt = attemptedAt; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(Instant nextRetryAt) { this.nextRetryAt = nextRetryAt; }
}
