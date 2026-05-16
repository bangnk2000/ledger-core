package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web.ReservationDtos.ReservationOutcomeDto;
import com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web.ReservationDtos.ReserveRequest;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.command.BalanceMutationRequest;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in.ReserveFundsUseCase;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.RequestIdentity;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/balances/reservations")
public class ReservationController {

	private final ReserveFundsUseCase reserveFundsUseCase;

	public ReservationController(ReserveFundsUseCase reserveFundsUseCase) {
		this.reserveFundsUseCase = reserveFundsUseCase;
	}

	@PostMapping
	public ResponseEntity<ReservationOutcomeDto> reserve(
			@RequestHeader("Idempotency-Key") String idempotencyKey,
			@RequestHeader("X-Requester-Scope") String requesterScope,
			@RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
			@Valid @RequestBody ReserveRequest request
	) {
		var result = reserveFundsUseCase.reserve(new BalanceMutationRequest(
			new RequestIdentity(requesterScope, idempotencyKey),
			BalanceMutationType.RESERVE,
			List.of(new AccountId(request.accountId())),
			new CurrencyCode(request.currency()),
			new MoneyAmount(new BigDecimal(request.amount())),
			request.direction(),
			new ActorContext(request.actor().actorId(), request.actor().actorType(), correlationId, request.actor().causationId()),
			request.businessReference(),
			request.expiresAt()));

		HttpStatus status = switch (result.outcome().outcome()) {
			case ACCEPTED -> HttpStatus.CREATED;
			case DUPLICATE -> HttpStatus.OK;
			case REJECTED -> HttpStatus.BAD_REQUEST;
			case CONFLICT -> HttpStatus.CONFLICT;
			case LOCKED -> HttpStatus.LOCKED;
			case FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
		};
		return ResponseEntity.status(status).body(ReservationOutcomeDto.from(result));
	}
}
