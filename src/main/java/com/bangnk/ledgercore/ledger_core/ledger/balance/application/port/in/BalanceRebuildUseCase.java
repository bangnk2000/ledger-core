package com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in;

import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceRebuildJob;
import java.util.Optional;
import java.util.UUID;

public interface BalanceRebuildUseCase {

	BalanceRebuildJob start(StartRebuildCommand command);

	Optional<BalanceRebuildJob> get(UUID jobId);

	record StartRebuildCommand(String scope, String replayContractVersion, boolean restartFromCheckpoint, String requestedBy) {
	}
}
