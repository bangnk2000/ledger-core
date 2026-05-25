package com.bangnk.ledgercore.ledger_core.balance.domain.model;

import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceRecoveryRepositoryPort.RebuildCheckpointRecord;
import java.time.Instant;
import java.util.UUID;

public record BalanceRebuildCheckpoint(
		UUID checkpointId,
		UUID jobId,
		String accountId,
		long ledgerSequence,
		long reservationSequence,
		Instant createdAt
) {
	public static BalanceRebuildCheckpoint create(UUID jobId, String accountId, long ledgerSequence, long reservationSequence, Instant now) {
		return new BalanceRebuildCheckpoint(UUID.randomUUID(), jobId, accountId, ledgerSequence, reservationSequence, now);
	}

	public static BalanceRebuildCheckpoint fromRecord(RebuildCheckpointRecord record) {
		return new BalanceRebuildCheckpoint(record.checkpointId(), record.jobId(), record.accountId(), record.ledgerSequence(),
			record.reservationSequence(), record.createdAt());
	}

	public RebuildCheckpointRecord toRecord() {
		return new RebuildCheckpointRecord(checkpointId, jobId, accountId, ledgerSequence, reservationSequence, createdAt);
	}
}
