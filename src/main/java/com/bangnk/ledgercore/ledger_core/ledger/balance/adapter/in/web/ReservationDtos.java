package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.BalanceApplicationErrors.BalanceOutcome;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in.ReserveFundsUseCase.ReserveFundsResult;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceActorType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceDirection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.UUID;

public final class ReservationDtos {

	private ReservationDtos() {
	}

	public record ReserveRequest(
			@NotBlank String accountId,
			@NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String currency,
			@NotBlank @Pattern(regexp = "^[0-9]+(\\.[0-9]{1,4})?$") String amount,
			@NotNull BalanceDirection direction,
			String businessReference,
			@NotNull Instant expiresAt,
			@NotNull @Valid ActorDto actor
	) {
	}

	public record ActorDto(
			@NotBlank String actorId,
			@NotNull BalanceActorType actorType,
			String causationId
	) {
	}

	public record ReservationOutcomeDto(
			String outcome,
			String code,
			String message,
			String requesterScope,
			String requestId,
			UUID reservationId,
			Instant occurredAt
	) {
		static ReservationOutcomeDto from(ReserveFundsResult result) {
			BalanceOutcome outcome = result.outcome();
			return new ReservationOutcomeDto(
				outcome.outcome().name(),
				outcome.code(),
				outcome.message(),
				outcome.requestIdentity().requesterScope(),
				outcome.requestIdentity().requestId(),
				result.reservationId(),
				outcome.occurredAt());
		}
	}
}
