package com.bangnk.ledgercore.ledger_core.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceMutationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "balance_idempotency_records")
@IdClass(BalanceIdempotencyJpaEntity.BalanceIdempotencyKey.class)
public class BalanceIdempotencyJpaEntity {
	@Id
	@Column(name = "requester_scope", nullable = false, length = 128)
	private String requesterScope;
	@Id
	@Column(name = "request_id", nullable = false, length = 128)
	private String requestId;
	@Column(name = "request_hash", nullable = false, length = 128)
	private String requestHash;
	@Column(name = "mutation_type", nullable = false, length = 32)
	@Enumerated(EnumType.STRING)
	private BalanceMutationType mutationType;
	@Column(name = "outcome", nullable = false, length = 32)
	private String outcome;
	@Column(name = "response_code", nullable = false, length = 128)
	private String responseCode;
	@Column(name = "response_payload")
	@JdbcTypeCode(SqlTypes.JSON)
	private String responsePayload;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	@Column(name = "last_seen_at", nullable = false)
	private Instant lastSeenAt;

	public BalanceIdempotencyJpaEntity() {
	}

	public BalanceIdempotencyJpaEntity(String requesterScope, String requestId, String requestHash, BalanceMutationType mutationType,
			String outcome, String responseCode, String responsePayload, Instant createdAt, Instant lastSeenAt) {
		this.requesterScope = requesterScope;
		this.requestId = requestId;
		this.requestHash = requestHash;
		this.mutationType = mutationType;
		this.outcome = outcome;
		this.responseCode = responseCode;
		this.responsePayload = responsePayload;
		this.createdAt = createdAt;
		this.lastSeenAt = lastSeenAt;
	}

	public String getRequesterScope() { return requesterScope; }
	public String getRequestId() { return requestId; }
	public String getRequestHash() { return requestHash; }
	public BalanceMutationType getMutationType() { return mutationType; }
	public String getOutcome() { return outcome; }
	public String getResponseCode() { return responseCode; }
	public String getResponsePayload() { return responsePayload; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getLastSeenAt() { return lastSeenAt; }

	public void setLastSeenAt(Instant lastSeenAt) {
		this.lastSeenAt = lastSeenAt;
	}

	public record BalanceIdempotencyKey(String requesterScope, String requestId) implements Serializable { }
}
