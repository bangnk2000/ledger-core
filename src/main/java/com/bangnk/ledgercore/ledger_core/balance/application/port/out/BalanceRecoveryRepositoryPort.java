package com.bangnk.ledgercore.ledger_core.balance.application.port.out;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.RebuildJobStatus;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReconciliationRecordStatus;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReconciliationSeverity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface BalanceRecoveryRepositoryPort {

	RebuildJobRecord saveRebuildJob(RebuildJobRecord job);

	Optional<RebuildJobRecord> findRebuildJob(UUID jobId);

	RebuildCheckpointRecord saveCheckpoint(RebuildCheckpointRecord checkpoint);

	Optional<RebuildCheckpointRecord> findLatestCheckpoint(UUID jobId);

	ReconciliationRecord saveReconciliationRecord(ReconciliationRecord record);

	record RebuildJobRecord(
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
	}

	record RebuildCheckpointRecord(
			UUID checkpointId,
			UUID jobId,
			String accountId,
			long ledgerSequence,
			long reservationSequence,
			Instant createdAt
	) {
	}

	record ReconciliationRecord(
			UUID reconciliationId,
			String accountScope,
			String expectedBalance,
			String actualBalance,
			String differenceSummary,
			ReconciliationSeverity severity,
			ReconciliationRecordStatus status,
			String investigationReference,
			Instant createdAt,
			Instant updatedAt
	) {
	}
}
