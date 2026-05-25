package com.bangnk.ledgercore.ledger_core.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceSnapshot.ConsistencyMode;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReconciliationStatus;
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
@Table(name = "balance_snapshots")
public class BalanceSnapshotJpaEntity {
	@Id
	@Column(name = "snapshot_id", nullable = false)
	private UUID snapshotId;
	@Column(name = "account_id", nullable = false, length = 128)
	private String accountId;
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
	@Column(name = "available_balance", nullable = false, precision = 19, scale = 4)
	private BigDecimal availableBalance;
	@Column(name = "snapshot_version", nullable = false)
	private long snapshotVersion;
	@Column(name = "as_of_sequence", nullable = false)
	private long asOfSequence;
	@Column(name = "as_of_time", nullable = false)
	private Instant asOfTime;
	@Column(name = "consistency_mode", nullable = false, length = 32)
	@Enumerated(EnumType.STRING)
	private ConsistencyMode consistencyMode;
	@Column(name = "reconciliation_status", length = 32)
	@Enumerated(EnumType.STRING)
	private ReconciliationStatus reconciliationStatus;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public BalanceSnapshotJpaEntity() {
	}

	public BalanceSnapshotJpaEntity(UUID snapshotId, String accountId, String currency, BigDecimal ledgerBalance, BigDecimal lockedAmount,
			BigDecimal pendingDebitAmount, BigDecimal pendingCreditAmount, BigDecimal availableBalance, long snapshotVersion,
			long asOfSequence, Instant asOfTime, ConsistencyMode consistencyMode, ReconciliationStatus reconciliationStatus, Instant createdAt) {
		this.snapshotId = snapshotId;
		this.accountId = accountId;
		this.currency = currency;
		this.ledgerBalance = ledgerBalance;
		this.lockedAmount = lockedAmount;
		this.pendingDebitAmount = pendingDebitAmount;
		this.pendingCreditAmount = pendingCreditAmount;
		this.availableBalance = availableBalance;
		this.snapshotVersion = snapshotVersion;
		this.asOfSequence = asOfSequence;
		this.asOfTime = asOfTime;
		this.consistencyMode = consistencyMode;
		this.reconciliationStatus = reconciliationStatus;
		this.createdAt = createdAt;
	}

	public UUID getSnapshotId() { return snapshotId; }
	public String getAccountId() { return accountId; }
	public String getCurrency() { return currency; }
	public BigDecimal getLedgerBalance() { return ledgerBalance; }
	public BigDecimal getLockedAmount() { return lockedAmount; }
	public BigDecimal getPendingDebitAmount() { return pendingDebitAmount; }
	public BigDecimal getPendingCreditAmount() { return pendingCreditAmount; }
	public BigDecimal getAvailableBalance() { return availableBalance; }
	public long getSnapshotVersion() { return snapshotVersion; }
	public long getAsOfSequence() { return asOfSequence; }
	public Instant getAsOfTime() { return asOfTime; }
	public ConsistencyMode getConsistencyMode() { return consistencyMode; }
	public ReconciliationStatus getReconciliationStatus() { return reconciliationStatus; }
	public Instant getCreatedAt() { return createdAt; }
}
