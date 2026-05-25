package com.bangnk.ledgercore.ledger_core.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReconciliationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "balance_state")
@IdClass(BalanceStateJpaEntity.BalanceStateId.class)
public class BalanceStateJpaEntity {
	@Id
	@Column(name = "account_id", nullable = false, length = 128)
	private String accountId;
	@Id
	@Column(name = "currency", nullable = false, length = 3)
	private String currency;
	@Column(name = "ledger_balance", nullable = false, precision = 19, scale = 4)
	private BigDecimal ledgerBalance;
	@Column(name = "locked_amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal lockedAmount;
	@Column(name = "pending_debit_amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal pendingDebitAmount;
	@Column(name = "pending_credit_amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal pendingCreditAmount;
	@Column(name = "version", nullable = false)
	private long version;
	@Column(name = "ledger_as_of_sequence", nullable = false)
	private long ledgerAsOfSequence;
	@Column(name = "reservation_as_of_sequence", nullable = false)
	private long reservationAsOfSequence;
	@Column(name = "reconciliation_status", nullable = false, length = 32)
	@Enumerated(EnumType.STRING)
	private ReconciliationStatus reconciliationStatus;
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public BalanceStateJpaEntity() {
	}

	public BalanceStateJpaEntity(String accountId, String currency, BigDecimal ledgerBalance, BigDecimal lockedAmount,
			BigDecimal pendingDebitAmount, BigDecimal pendingCreditAmount, long version, long ledgerAsOfSequence,
			long reservationAsOfSequence, ReconciliationStatus reconciliationStatus, Instant updatedAt) {
		this.accountId = accountId;
		this.currency = currency;
		this.ledgerBalance = ledgerBalance;
		this.lockedAmount = lockedAmount;
		this.pendingDebitAmount = pendingDebitAmount;
		this.pendingCreditAmount = pendingCreditAmount;
		this.version = version;
		this.ledgerAsOfSequence = ledgerAsOfSequence;
		this.reservationAsOfSequence = reservationAsOfSequence;
		this.reconciliationStatus = reconciliationStatus;
		this.updatedAt = updatedAt;
	}

	public String getAccountId() { return accountId; }
	public String getCurrency() { return currency; }
	public BigDecimal getLedgerBalance() { return ledgerBalance; }
	public BigDecimal getLockedAmount() { return lockedAmount; }
	public BigDecimal getPendingDebitAmount() { return pendingDebitAmount; }
	public BigDecimal getPendingCreditAmount() { return pendingCreditAmount; }
	public long getVersion() { return version; }
	public long getLedgerAsOfSequence() { return ledgerAsOfSequence; }
	public long getReservationAsOfSequence() { return reservationAsOfSequence; }
	public ReconciliationStatus getReconciliationStatus() { return reconciliationStatus; }
	public Instant getUpdatedAt() { return updatedAt; }

	public record BalanceStateId(String accountId, String currency) implements Serializable { }
}
