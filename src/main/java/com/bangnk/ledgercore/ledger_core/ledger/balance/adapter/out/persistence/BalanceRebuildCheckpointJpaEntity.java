package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "balance_rebuild_checkpoints")
public class BalanceRebuildCheckpointJpaEntity {
	@Id
	@Column(name = "checkpoint_id", nullable = false)
	private UUID checkpointId;
	@Column(name = "job_id", nullable = false)
	private UUID jobId;
	@Column(name = "account_id", length = 128)
	private String accountId;
	@Column(name = "ledger_sequence", nullable = false)
	private long ledgerSequence;
	@Column(name = "reservation_sequence", nullable = false)
	private long reservationSequence;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public BalanceRebuildCheckpointJpaEntity() {}

	public BalanceRebuildCheckpointJpaEntity(UUID checkpointId, UUID jobId, String accountId, long ledgerSequence, long reservationSequence,
			Instant createdAt) {
		this.checkpointId = checkpointId;
		this.jobId = jobId;
		this.accountId = accountId;
		this.ledgerSequence = ledgerSequence;
		this.reservationSequence = reservationSequence;
		this.createdAt = createdAt;
	}

	public UUID getCheckpointId() { return checkpointId; }
	public UUID getJobId() { return jobId; }
	public String getAccountId() { return accountId; }
	public long getLedgerSequence() { return ledgerSequence; }
	public long getReservationSequence() { return reservationSequence; }
	public Instant getCreatedAt() { return createdAt; }
}
