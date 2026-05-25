package com.bangnk.ledgercore.ledger_core.balance.application.port.in;

import com.bangnk.ledgercore.ledger_core.balance.application.BalanceApplicationErrors.BalanceOutcome;
import com.bangnk.ledgercore.ledger_core.balance.application.command.BalanceMutationRequest;
import java.util.UUID;

public interface ReserveFundsUseCase {

	ReserveFundsResult reserve(BalanceMutationRequest request);

	record ReserveFundsResult(BalanceOutcome outcome, UUID reservationId) {
	}
}
