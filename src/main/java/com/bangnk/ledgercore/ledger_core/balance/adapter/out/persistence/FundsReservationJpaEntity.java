package com.bangnk.ledgercore.ledger_core.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "funds_reservations")
public class FundsReservationJpaEntity {
	@Id
	@Column(name = "reservation_id", nullable = false)
	private UUID reservationId;
	@Column(name = "requester_scope", nullable = false, length = 128)
	private String requesterScope;
	@Column(name = "request_id", nullable = false, length = 128)
	private String requestId;
	@Column(name = "account_id", nullable = false, length = 128)
	private String accountId;
	@Column(name = "currency", nullable = false, length = 3)
	private String currency;
	@Column(name = "direction", nullable = false, length = 16)
	@Enumerated(EnumType.STRING)
	private BalanceDirection direction;
	@Column(name = "amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;
	@Column(name = "business_reference", length = 128)
	private String businessReference;
	@Column(name = "status", nullable = false, length = 32)
	@Enumerated(EnumType.STRING)
	private ReservationStatus status;
	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;
	@Column(name = "ledger_transaction_id", length = 128)
	private String ledgerTransactionId;
	@Column(name = "confirmation_reference", length = 128)
	private String confirmationReference;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public FundsReservationJpaEntity() {
	}

	public FundsReservationJpaEntity(UUID reservationId, String requesterScope, String requestId, String accountId, String currency,
			BalanceDirection direction, BigDecimal amount, String businessReference, ReservationStatus status, Instant expiresAt,
			String ledgerTransactionId, String confirmationReference, Instant createdAt, Instant updatedAt) {
		this.reservationId = reservationId;
		this.requesterScope = requesterScope;
		this.requestId = requestId;
		this.accountId = accountId;
		this.currency = currency;
		this.direction = direction;
		this.amount = amount;
		this.businessReference = businessReference;
		this.status = status;
		this.expiresAt = expiresAt;
		this.ledgerTransactionId = ledgerTransactionId;
		this.confirmationReference = confirmationReference;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UUID getReservationId() { return reservationId; }
	public String getRequesterScope() { return requesterScope; }
	public String getRequestId() { return requestId; }
	public String getAccountId() { return accountId; }
	public String getCurrency() { return currency; }
	public BalanceDirection getDirection() { return direction; }
	public BigDecimal getAmount() { return amount; }
	public String getBusinessReference() { return businessReference; }
	public ReservationStatus getStatus() { return status; }
	public Instant getExpiresAt() { return expiresAt; }
	public String getLedgerTransactionId() { return ledgerTransactionId; }
	public String getConfirmationReference() { return confirmationReference; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
