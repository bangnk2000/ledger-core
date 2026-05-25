package com.bangnk.ledgercore.ledger_core.balance.application.port.in;

import com.bangnk.ledgercore.ledger_core.balance.application.BalanceApplicationErrors.BalanceOutcome;
import com.bangnk.ledgercore.ledger_core.balance.adapter.in.web.ReservationDtos.ConfirmReservationRequest.FinalizationType;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import java.util.UUID;

public interface ConfirmReservationUseCase {

	ReservationLifecycleResult confirm(
		UUID reservationId,
		RequestIdentity requestIdentity,
		FinalizationType finalizationType,
		String ledgerTransactionId,
		ActorContext actorContext);

	record ReservationLifecycleResult(BalanceOutcome outcome, UUID reservationId) {
	}
}
