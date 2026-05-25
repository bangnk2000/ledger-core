package com.bangnk.ledgercore.ledger_core.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReconciliationRecordStatus;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReconciliationSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "balance_reconciliation_records")
public class BalanceReconciliationRecordJpaEntity {
	@Id
	@Column(name = "reconciliation_id", nullable = false)
	private UUID reconciliationId;
	@Column(name = "account_scope", nullable = false)
	private String accountScope;
	@Column(name = "expected_balance", nullable = false, columnDefinition = "jsonb")
	@JdbcTypeCode(SqlTypes.JSON)
	private String expectedBalance;
	@Column(name = "actual_balance", nullable = false, columnDefinition = "jsonb")
	@JdbcTypeCode(SqlTypes.JSON)
	private String actualBalance;
	@Column(name = "difference_summary", nullable = false, columnDefinition = "jsonb")
	@JdbcTypeCode(SqlTypes.JSON)
	private String differenceSummary;
	@Column(name = "severity", nullable = false, length = 32)
	@Enumerated(EnumType.STRING)
	private ReconciliationSeverity severity;
	@Column(name = "status", nullable = false, length = 32)
	@Enumerated(EnumType.STRING)
	private ReconciliationRecordStatus status;
	@Column(name = "investigation_reference", length = 128)
	private String investigationReference;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public BalanceReconciliationRecordJpaEntity() {}

	public BalanceReconciliationRecordJpaEntity(UUID reconciliationId, String accountScope, String expectedBalance, String actualBalance,
			String differenceSummary, ReconciliationSeverity severity, ReconciliationRecordStatus status, String investigationReference,
			Instant createdAt, Instant updatedAt) {
		this.reconciliationId = reconciliationId;
		this.accountScope = accountScope;
		this.expectedBalance = expectedBalance;
		this.actualBalance = actualBalance;
		this.differenceSummary = differenceSummary;
		this.severity = severity;
		this.status = status;
		this.investigationReference = investigationReference;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UUID getReconciliationId() { return reconciliationId; }
	public String getAccountScope() { return accountScope; }
	public String getExpectedBalance() { return expectedBalance; }
	public String getActualBalance() { return actualBalance; }
	public String getDifferenceSummary() { return differenceSummary; }
	public ReconciliationSeverity getSeverity() { return severity; }
	public ReconciliationRecordStatus getStatus() { return status; }
	public String getInvestigationReference() { return investigationReference; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
