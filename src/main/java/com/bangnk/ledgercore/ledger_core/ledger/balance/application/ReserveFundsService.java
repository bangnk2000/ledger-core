package com.bangnk.ledgercore.ledger_core.ledger.balance.application;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.BalanceApplicationErrors.BalanceOutcome;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.BalanceApplicationErrors.BalanceOutcomeType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.BalanceIdempotencyService.IdempotencyDecision;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.command.BalanceMutationRequest;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in.ReserveFundsUseCase;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.BalanceIdempotencyRepositoryPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.BalanceStateRepositoryPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.BalanceTransactionPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.FundsReservationRepositoryPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceState;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.FundsReservation;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.RequestIdentity.RequestHash;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ReserveFundsService implements ReserveFundsUseCase {

	private final BalanceStateRepositoryPort balanceStateRepository;
	private final FundsReservationRepositoryPort reservationRepository;
	private final BalanceIdempotencyRepositoryPort idempotencyRepository;
	private final BalanceTransactionPort transactionPort;
	private final ProtectedWriteRetryExecutor retryExecutor;
	private final BalanceIdempotencyService idempotencyService;
	private final BalanceConsistencyGuard consistencyGuard;
	private final BalanceObservability observability;
	private final Clock clock;

	public ReserveFundsService(
			BalanceStateRepositoryPort balanceStateRepository,
			FundsReservationRepositoryPort reservationRepository,
			BalanceIdempotencyRepositoryPort idempotencyRepository,
			BalanceTransactionPort transactionPort,
			ProtectedWriteRetryExecutor retryExecutor,
			BalanceIdempotencyService idempotencyService,
			BalanceConsistencyGuard consistencyGuard,
			BalanceObservability observability,
			Clock clock
	) {
		this.balanceStateRepository = balanceStateRepository;
		this.reservationRepository = reservationRepository;
		this.idempotencyRepository = idempotencyRepository;
		this.transactionPort = transactionPort;
		this.retryExecutor = retryExecutor;
		this.idempotencyService = idempotencyService;
		this.consistencyGuard = consistencyGuard;
		this.observability = observability;
		this.clock = clock;
	}

	@Override
	public ReserveFundsResult reserve(BalanceMutationRequest request) {
		consistencyGuard.assertWritable(request.requestIdentity());
		return retryExecutor.execute(() -> transactionPort.withinProtectedWrite(() -> reserveInTransaction(request)));
	}

	private ReserveFundsResult reserveInTransaction(BalanceMutationRequest request) {
		Instant now = Instant.now(clock);
		RequestHash requestHash = new RequestHash(request.requestIdentity().requestId() + "|" + request.amount().value().toPlainString());
		Optional<IdempotencyDecision> decision = idempotencyService.evaluate(
			request.requestIdentity(),
			requestHash,
			BalanceMutationType.RESERVE,
			"BALANCE_RESERVATION_DUPLICATE",
			"Duplicate reservation request",
			"Request identity already used for a different reservation intent",
			now);
		if (decision.isPresent()) {
			IdempotencyDecision evaluated = decision.orElseThrow();
			observability.recordReserveOutcome(evaluated.outcome().outcome());
			observability.recordDuplicate();
			return new ReserveFundsResult(evaluated.outcome(), payloadReservationId(evaluated.responsePayload()));
		}

		var key = new BalanceStateRepositoryPort.BalanceStateKey(request.accountIds().getFirst(), request.currency());
		BalanceState state = balanceStateRepository.findByAccountAndCurrencyForUpdate(key.accountId(), key.currency())
			.map(BalanceState::fromRecord)
			.orElseGet(() -> BalanceState.empty(key.accountId(), key.currency()));
		BalanceState nextState;
		try {
			nextState = state.reserve(request.direction(), request.amount(), now);
		} catch (IllegalArgumentException ex) {
			BalanceOutcome rejected = BalanceOutcome.rejected("BALANCE_INSUFFICIENT_FUNDS", ex.getMessage(), request.requestIdentity());
			idempotencyRepository.save(new BalanceIdempotencyRepositoryPort.IdempotencyRecord(
				request.requestIdentity(),
				requestHash,
				BalanceMutationType.RESERVE,
				rejected.outcome().name(),
				rejected.code(),
				null,
				now,
				now));
			observability.recordReserveOutcome(BalanceOutcomeType.REJECTED);
			return new ReserveFundsResult(rejected, null);
		}
		balanceStateRepository.save(nextState.toRecord());
		FundsReservation reservation = FundsReservation.createActive(
			request.requestIdentity(),
			key.accountId(),
			key.currency(),
			request.direction(),
			request.amount(),
			request.businessReference(),
			request.expiresAt(),
			now);
		reservationRepository.save(reservation.toRecord());

		BalanceOutcome accepted = BalanceOutcome.accepted("BALANCE_RESERVED", "Funds reserved successfully", request.requestIdentity());
		idempotencyRepository.save(new BalanceIdempotencyRepositoryPort.IdempotencyRecord(
			request.requestIdentity(),
			requestHash,
			BalanceMutationType.RESERVE,
			accepted.outcome().name(),
			accepted.code(),
			"\"" + reservation.reservationId() + "\"",
			now,
			now));
		observability.recordReserveOutcome(BalanceOutcomeType.ACCEPTED);
		return new ReserveFundsResult(accepted, reservation.reservationId());
	}

	private UUID payloadReservationId(String responsePayload) {
		if (responsePayload == null || responsePayload.isBlank()) {
			return null;
		}
		return UUID.fromString(responsePayload.replace("\"", ""));
	}
}
