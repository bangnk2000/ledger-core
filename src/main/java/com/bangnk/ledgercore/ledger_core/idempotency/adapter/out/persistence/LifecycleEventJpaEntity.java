package com.bangnk.ledgercore.ledger_core.idempotency.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_lifecycle_events")
public class LifecycleEventJpaEntity {
    @Id
    private UUID id;
    @Column(name = "record_id", nullable = false) private UUID recordId;
    @Column(name = "event_type", nullable = false) private String eventType;
    @Column(name = "recorded_at", nullable = false) private Instant recordedAt;
    @Column(name = "actor_type", nullable = false) private String actorType;
    @Column(name = "actor_id", nullable = false) private String actorId;
    @Column(name = "details") private String details;

    public LifecycleEventJpaEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public UUID getRecordId() { return recordId; }
    public void setRecordId(UUID v) { this.recordId = v; }
    public String getEventType() { return eventType; }
    public void setEventType(String v) { this.eventType = v; }
    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant v) { this.recordedAt = v; }
    public String getActorType() { return actorType; }
    public void setActorType(String v) { this.actorType = v; }
    public String getActorId() { return actorId; }
    public void setActorId(String v) { this.actorId = v; }
    public String getDetails() { return details; }
    public void setDetails(String v) { this.details = v; }
}
