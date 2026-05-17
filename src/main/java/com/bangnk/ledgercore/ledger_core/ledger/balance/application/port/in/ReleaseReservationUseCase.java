package com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.BalanceApplicationErrors.BalanceOutcome;
import com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web.ReservationDtos.ReleaseReservationRequest.ReleaseReason;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.RequestIdentity;
import java.util.UUID;

public interface ReleaseReservationUseCase {

	ReservationReleaseResult release(
		UUID reservationId,
		RequestIdentity requestIdentity,
		ReleaseReason releaseReason,
		ActorContext actorContext);

	record ReservationReleaseResult(BalanceOutcome outcome, UUID reservationId) {
	}
}
