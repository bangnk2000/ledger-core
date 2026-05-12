package com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.PostingOutcomeType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.TransactionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class LedgerJpaEntities {

	private LedgerJpaEntities() {
	}

	@Entity
	@Table(name = "ledger_transactions")
	public static class LedgerTransactionJpaEntity {
		@Id
		private UUID id;
		@Column(nullable = false)
		private String requesterScope;
		@Column(nullable = false)
		private String requestId;
		@Column(nullable = false)
		private String requestHash;
		@Enumerated(EnumType.STRING)
		@Column(nullable = false)
		private TransactionStatus status;
		private String businessReference;
		private String description;
		@JdbcTypeCode(SqlTypes.JSON)
		@Column(columnDefinition = "jsonb")
		private String metadata;
		private String correlationId;
		private String causationId;
		@Column(nullable = false)
		private String actorId;
		@Enumerated(EnumType.STRING)
		@Column(nullable = false)
		private ActorType actorType;
		private Instant submittedAt;
		private Instant postedAt;
		private String rejectionCode;
		private String rejectionReason;
		@Column(nullable = false)
		private Instant createdAt;

		public LedgerTransactionJpaEntity() {
		}

		public LedgerTransactionJpaEntity(UUID id, String requesterScope, String requestId, String requestHash, TransactionStatus status,
				String businessReference, String description, String metadata, String correlationId, String causationId,
				String actorId, ActorType actorType, Instant submittedAt, Instant postedAt, String rejectionCode,
				String rejectionReason, Instant createdAt) {
			this.id = id;
			this.requesterScope = requesterScope;
			this.requestId = requestId;
			this.requestHash = requestHash;
			this.status = status;
			this.businessReference = businessReference;
			this.description = description;
			this.metadata = metadata;
			this.correlationId = correlationId;
			this.causationId = causationId;
			this.actorId = actorId;
			this.actorType = actorType;
			this.submittedAt = submittedAt;
			this.postedAt = postedAt;
			this.rejectionCode = rejectionCode;
			this.rejectionReason = rejectionReason;
			this.createdAt = createdAt;
		}

		public UUID getId() { return id; }
		public String getRequesterScope() { return requesterScope; }
		public String getRequestId() { return requestId; }
		public String getRequestHash() { return requestHash; }
		public TransactionStatus getStatus() { return status; }
		public String getBusinessReference() { return businessReference; }
		public String getDescription() { return description; }
		public String getMetadata() { return metadata; }
		public String getCorrelationId() { return correlationId; }
		public String getCausationId() { return causationId; }
		public String getActorId() { return actorId; }
		public ActorType getActorType() { return actorType; }
		public Instant getSubmittedAt() { return submittedAt; }
		public Instant getPostedAt() { return postedAt; }
		public String getRejectionCode() { return rejectionCode; }
		public String getRejectionReason() { return rejectionReason; }
		public Instant getCreatedAt() { return createdAt; }
	}

	@Entity
	@Table(name = "ledger_entries")
	public static class LedgerEntryJpaEntity {
		@Id
		private UUID id;
		@Column(nullable = false, updatable = false)
		private UUID transactionId;
		@Column(nullable = false, updatable = false)
		private String lineId;
		@Column(nullable = false, updatable = false)
		private String accountId;
		@Enumerated(EnumType.STRING)
		@Column(nullable = false, updatable = false)
		private Direction direction;
		@Column(nullable = false, updatable = false, precision = 19, scale = 4)
		private BigDecimal amount;
		@Column(nullable = false, updatable = false, length = 3, columnDefinition = "char(3)")
		@JdbcTypeCode(Types.CHAR)
		private String currency;
		@JdbcTypeCode(SqlTypes.JSON)
		@Column(columnDefinition = "jsonb")
		private String entryMetadata;
		@Column(nullable = false, updatable = false)
		private Instant postedAt;
		@Column(nullable = false, updatable = false)
		private Instant createdAt;

		public LedgerEntryJpaEntity() {
		}

		public LedgerEntryJpaEntity(UUID id, UUID transactionId, String lineId, String accountId, Direction direction,
				BigDecimal amount, String currency, String entryMetadata, Instant postedAt, Instant createdAt) {
			this.id = id;
			this.transactionId = transactionId;
			this.lineId = lineId;
			this.accountId = accountId;
			this.direction = direction;
			this.amount = amount;
			this.currency = currency;
			this.entryMetadata = entryMetadata;
			this.postedAt = postedAt;
			this.createdAt = createdAt;
		}

		public UUID getId() { return id; }
		public UUID getTransactionId() { return transactionId; }
		public String getLineId() { return lineId; }
		public String getAccountId() { return accountId; }
		public Direction getDirection() { return direction; }
		public BigDecimal getAmount() { return amount; }
		public String getCurrency() { return currency; }
		public String getEntryMetadata() { return entryMetadata; }
		public Instant getPostedAt() { return postedAt; }
		public Instant getCreatedAt() { return createdAt; }
	}

	@Entity
	@Table(name = "ledger_idempotency_records")
	@IdClass(IdempotencyRecordKey.class)
	public static class IdempotencyRecordJpaEntity {
		@Id
		private String requesterScope;
		@Id
		private String requestId;
		@Column(nullable = false)
		private String requestHash;
		@Enumerated(EnumType.STRING)
		@Column(nullable = false)
		private PostingOutcomeType outcome;
		private UUID transactionId;
		@Column(nullable = false)
		private String responseCode;
		@Column(nullable = false)
		private Instant createdAt;
		@Column(nullable = false)
		private Instant lastSeenAt;

		public IdempotencyRecordJpaEntity() {
		}

		public IdempotencyRecordJpaEntity(String requesterScope, String requestId, String requestHash, PostingOutcomeType outcome,
				UUID transactionId, String responseCode, Instant createdAt, Instant lastSeenAt) {
			this.requesterScope = requesterScope;
			this.requestId = requestId;
			this.requestHash = requestHash;
			this.outcome = outcome;
			this.transactionId = transactionId;
			this.responseCode = responseCode;
			this.createdAt = createdAt;
			this.lastSeenAt = lastSeenAt;
		}

		public String getRequesterScope() { return requesterScope; }
		public String getRequestId() { return requestId; }
		public String getRequestHash() { return requestHash; }
		public PostingOutcomeType getOutcome() { return outcome; }
		public UUID getTransactionId() { return transactionId; }
		public String getResponseCode() { return responseCode; }
		public Instant getCreatedAt() { return createdAt; }
		public Instant getLastSeenAt() { return lastSeenAt; }
	}

	public static final class IdempotencyRecordKey implements java.io.Serializable {
		private String requesterScope;
		private String requestId;

		public IdempotencyRecordKey() {
		}

		public IdempotencyRecordKey(String requesterScope, String requestId) {
			this.requesterScope = requesterScope;
			this.requestId = requestId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof IdempotencyRecordKey that)) {
				return false;
			}
			return Objects.equals(requesterScope, that.requesterScope) && Objects.equals(requestId, that.requestId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(requesterScope, requestId);
		}
	}
}
