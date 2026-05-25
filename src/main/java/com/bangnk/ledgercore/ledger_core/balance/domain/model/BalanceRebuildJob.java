package com.bangnk.ledgercore.ledger_core.balance.domain.model;

import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceRecoveryRepositoryPort.RebuildJobRecord;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.RebuildJobStatus;
import java.time.Instant;
import java.util.UUID;

public record BalanceRebuildJob(
		UUID jobId,
		String scope,
		String replayContractVersion,
		RebuildJobStatus status,
		String checkpointToken,
		long processedRecordCount,
		Instant startedAt,
		Instant finishedAt,
		String requestedBy,
		String failureReason
) {
	public static BalanceRebuildJob start(String scope, String replayContractVersion, String requestedBy, Instant now) {
		return new BalanceRebuildJob(UUID.randomUUID(), scope, replayContractVersion, RebuildJobStatus.RUNNING, null, 0L, now, null, requestedBy, null);
	}

	public static BalanceRebuildJob fromRecord(RebuildJobRecord record) {
		return new BalanceRebuildJob(
			record.jobId(),
			record.scope(),
			record.replayContractVersion(),
			record.status(),
			record.checkpointToken(),
			record.processedRecordCount(),
			record.startedAt(),
			record.finishedAt(),
			record.requestedBy(),
			record.failureReason());
	}

	public BalanceRebuildJob complete(String checkpointToken, long processedRecordCount, Instant now) {
		return new BalanceRebuildJob(
			jobId, scope, replayContractVersion, RebuildJobStatus.SUCCEEDED, checkpointToken, processedRecordCount, startedAt, now, requestedBy, null);
	}

	public RebuildJobRecord toRecord() {
		return new RebuildJobRecord(
			jobId,
			scope,
			replayContractVersion,
			status,
			checkpointToken,
			processedRecordCount,
			startedAt,
			finishedAt,
			requestedBy,
			failureReason);
	}
}
