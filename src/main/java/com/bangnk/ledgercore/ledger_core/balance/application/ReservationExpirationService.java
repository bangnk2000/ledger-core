package com.bangnk.ledgercore.ledger_core.balance.application;

import com.bangnk.ledgercore.ledger_core.balance.adapter.in.web.ReservationDtos.ReleaseReservationRequest.ReleaseReason;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceActorType;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class ReservationExpirationService {

	private final ReservationLifecycleService reservationLifecycleService;
	private final Clock clock;

	public ReservationExpirationService(ReservationLifecycleService reservationLifecycleService, Clock clock) {
		this.reservationLifecycleService = reservationLifecycleService;
		this.clock = clock;
	}

	public void expireReservation(String reservationId, String requesterScope) {
		Instant now = Instant.now(clock);
		String requestId = "expire-" + reservationId + "-" + now.toEpochMilli();
		reservationLifecycleService.release(
			java.util.UUID.fromString(reservationId),
			new RequestIdentity(requesterScope, requestId),
			ReleaseReason.EXPIRED,
			new ActorContext("reservation-expiration", BalanceActorType.SYSTEM, "exp-" + requestId, null));
	}
}
