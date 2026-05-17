package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.RebuildJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "balance_rebuild_jobs")
public class BalanceRebuildJobJpaEntity {
	@Id
	@Column(name = "job_id", nullable = false)
	private UUID jobId;
	@Column(name = "scope", nullable = false)
	private String scope;
	@Column(name = "replay_contract_version", nullable = false, length = 32)
	private String replayContractVersion;
	@Column(name = "status", nullable = false, length = 32)
	@Enumerated(EnumType.STRING)
	private RebuildJobStatus status;
	@Column(name = "checkpoint_token")
	private String checkpointToken;
	@Column(name = "processed_record_count", nullable = false)
	private long processedRecordCount;
	@Column(name = "started_at")
	private Instant startedAt;
	@Column(name = "finished_at")
	private Instant finishedAt;
	@Column(name = "requested_by", nullable = false, length = 128)
	private String requestedBy;
	@Column(name = "failure_reason", length = 512)
	private String failureReason;

	public BalanceRebuildJobJpaEntity() {}

	public BalanceRebuildJobJpaEntity(UUID jobId, String scope, String replayContractVersion, RebuildJobStatus status, String checkpointToken,
			long processedRecordCount, Instant startedAt, Instant finishedAt, String requestedBy, String failureReason) {
		this.jobId = jobId;
		this.scope = scope;
		this.replayContractVersion = replayContractVersion;
		this.status = status;
		this.checkpointToken = checkpointToken;
		this.processedRecordCount = processedRecordCount;
		this.startedAt = startedAt;
		this.finishedAt = finishedAt;
		this.requestedBy = requestedBy;
		this.failureReason = failureReason;
	}

	public UUID getJobId() { return jobId; }
	public String getScope() { return scope; }
	public String getReplayContractVersion() { return replayContractVersion; }
	public RebuildJobStatus getStatus() { return status; }
	public String getCheckpointToken() { return checkpointToken; }
	public long getProcessedRecordCount() { return processedRecordCount; }
	public Instant getStartedAt() { return startedAt; }
	public Instant getFinishedAt() { return finishedAt; }
	public String getRequestedBy() { return requestedBy; }
	public String getFailureReason() { return failureReason; }
}
