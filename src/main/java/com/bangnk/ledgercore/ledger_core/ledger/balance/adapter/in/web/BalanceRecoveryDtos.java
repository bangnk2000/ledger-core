package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in.BalanceRebuildUseCase.StartRebuildCommand;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in.BalanceReconciliationUseCase.ReconciliationAccepted;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in.BalanceReconciliationUseCase.StartReconciliationCommand;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceRebuildJob;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public final class BalanceRecoveryDtos {

	private BalanceRecoveryDtos() {}

	public record StartRebuildRequest(
			@NotBlank String scope,
			@NotBlank String replayContractVersion,
			boolean restartFromCheckpoint
	) {
		StartRebuildCommand toCommand() {
			return new StartRebuildCommand(scope, replayContractVersion, restartFromCheckpoint, "api");
		}
	}

	public record RebuildJobDto(
			String jobId,
			String status,
			String scope,
			String replayContractVersion,
			String checkpointToken,
			long processedRecordCount,
			Instant startedAt,
			Instant finishedAt,
			String failureReason
	) {
		static RebuildJobDto from(BalanceRebuildJob job) {
			return new RebuildJobDto(
				job.jobId().toString(),
				job.status().name(),
				job.scope(),
				job.replayContractVersion(),
				job.checkpointToken(),
				job.processedRecordCount(),
				job.startedAt(),
				job.finishedAt(),
				job.failureReason());
		}
	}

	public record StartReconciliationRequest(@NotBlank String scope, @NotNull Boolean rebuildIfDriftDetected) {
		StartReconciliationCommand toCommand() {
			return new StartReconciliationCommand(scope, rebuildIfDriftDetected);
		}
	}

	public record ReconciliationRunAcceptedDto(String reconciliationRunId, String status) {
		static ReconciliationRunAcceptedDto from(ReconciliationAccepted accepted) {
			return new ReconciliationRunAcceptedDto(accepted.reconciliationRunId().toString(), accepted.status());
		}
	}
}
