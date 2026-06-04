package com.bangnk.ledgercore.ledger_core.balance.application;

import com.bangnk.ledgercore.ledger_core.audit.application.port.in.AuditCaptureUseCase;
import com.bangnk.ledgercore.ledger_core.balance.adapter.in.web.ReservationDtos.ConfirmReservationRequest.FinalizationType;
import com.bangnk.ledgercore.ledger_core.balance.adapter.in.web.ReservationDtos.ReleaseReservationRequest.ReleaseReason;
import com.bangnk.ledgercore.ledger_core.balance.application.BalanceApplicationErrors.BalanceOutcome;
import com.bangnk.ledgercore.ledger_core.balance.application.BalanceApplicationErrors.BalanceOutcomeType;
import com.bangnk.ledgercore.ledger_core.balance.application.BalanceIdempotencyService.IdempotencyDecision;
import com.bangnk.ledgercore.ledger_core.balance.application.port.in.ConfirmReservationUseCase;
import com.bangnk.ledgercore.ledger_core.balance.application.port.in.ReleaseReservationUseCase;
import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceIdempotencyRepositoryPort;
import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceStateRepositoryPort;
import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceTransactionPort;
import com.bangnk.ledgercore.ledger_core.balance.application.port.out.FundsReservationRepositoryPort;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReservationStatus;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceState;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.FundsReservation;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity.RequestHash;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReservationLifecycleService implements ConfirmReservationUseCase, ReleaseReservationUseCase {

	private final FundsReservationRepositoryPort reservationRepository;
	private final BalanceStateRepositoryPort balanceStateRepository;
	private final BalanceIdempotencyRepositoryPort idempotencyRepository;
	private final BalanceTransactionPort transactionPort;
	private final ProtectedWriteRetryExecutor retryExecutor;
	private final BalanceIdempotencyService idempotencyService;
	private final BalanceConsistencyGuard consistencyGuard;
	private final BalanceObservability observability;
	private final AuditCaptureUseCase auditCaptureUseCase;
	private final BalanceAuditTranslator auditTranslator;
	private final Clock clock;

	@Autowired
	public ReservationLifecycleService(
			FundsReservationRepositoryPort reservationRepository,
			BalanceStateRepositoryPort balanceStateRepository,
			BalanceIdempotencyRepositoryPort idempotencyRepository,
			BalanceTransactionPort transactionPort,
			ProtectedWriteRetryExecutor retryExecutor,
			BalanceIdempotencyService idempotencyService,
			BalanceConsistencyGuard consistencyGuard,
			BalanceObservability observability,
			AuditCaptureUseCase auditCaptureUseCase,
			BalanceAuditTranslator auditTranslator,
			Clock clock
	) {
		this.reservationRepository = reservationRepository;
		this.balanceStateRepository = balanceStateRepository;
		this.idempotencyRepository = idempotencyRepository;
		this.transactionPort = transactionPort;
		this.retryExecutor = retryExecutor;
		this.idempotencyService = idempotencyService;
		this.consistencyGuard = consistencyGuard;
		this.observability = observability;
		this.auditCaptureUseCase = auditCaptureUseCase;
		this.auditTranslator = auditTranslator;
		this.clock = clock;
	}

	public ReservationLifecycleService(
			FundsReservationRepositoryPort reservationRepository,
			BalanceStateRepositoryPort balanceStateRepository,
			BalanceIdempotencyRepositoryPort idempotencyRepository,
			BalanceTransactionPort transactionPort,
			ProtectedWriteRetryExecutor retryExecutor,
			BalanceIdempotencyService idempotencyService,
			BalanceConsistencyGuard consistencyGuard,
			BalanceObservability observability,
			Clock clock
	) {
		this(
			reservationRepository,
			balanceStateRepository,
			idempotencyRepository,
			transactionPort,
			retryExecutor,
			idempotencyService,
			consistencyGuard,
			observability,
			command -> new AuditCaptureUseCase.CaptureResult(UUID.randomUUID(), null),
			new BalanceAuditTranslator(),
			clock
		);
	}

	@Override
	public ReservationLifecycleResult confirm(UUID reservationId, RequestIdentity requestIdentity, FinalizationType finalizationType, String ledgerTransactionId,
			ActorContext actorContext) {
		consistencyGuard.assertWritable(requestIdentity);
		return retryExecutor.execute(
			() -> transactionPort.withinProtectedWrite(() -> doConfirm(reservationId, requestIdentity, finalizationType, ledgerTransactionId, actorContext)));
	}

	@Override
	public ReservationReleaseResult release(UUID reservationId, RequestIdentity requestIdentity, ReleaseReason releaseReason, ActorContext actorContext) {
		consistencyGuard.assertWritable(requestIdentity);
		return retryExecutor.execute(() -> transactionPort.withinProtectedWrite(() -> doRelease(reservationId, requestIdentity, releaseReason, actorContext)));
	}

	private ReservationLifecycleResult doConfirm(UUID reservationId, RequestIdentity requestIdentity, FinalizationType finalizationType, String ledgerTransactionId,
			ActorContext actorContext) {
		Instant now = Instant.now(clock);
		RequestHash requestHash = new RequestHash(reservationId + "|" + finalizationType + "|" + ledgerTransactionId);
		Optional<IdempotencyDecision> decision = idempotencyService.evaluate(
			requestIdentity,
			requestHash,
			BalanceMutationType.CONFIRM,
			"BALANCE_RESERVATION_CONFIRM_DUPLICATE",
			"Duplicate confirmation request",
			"Request identity already used for a different confirmation intent",
			now);
		if (decision.isPresent()) {
			IdempotencyDecision evaluated = decision.orElseThrow();
			observability.recordLifecycleOutcome(evaluated.outcome().outcome());
			observability.recordDuplicate();
			captureLifecycleAuditSafely(
				reservationId,
				requestIdentity,
				actorContext,
				BalanceMutationType.CONFIRM,
				now,
				"CONFIRM_DUPLICATE",
				ledgerTransactionId
			);
			return new ReservationLifecycleResult(evaluated.outcome(), reservationId);
		}

		FundsReservation reservation = reservationRepository.findByIdForUpdate(reservationId)
			.map(FundsReservation::fromRecord)
			.orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
		if (reservation.status() == ReservationStatus.CONFIRMED) {
			var duplicate = BalanceOutcome.duplicate("BALANCE_RESERVATION_ALREADY_CONFIRMED", "Reservation already confirmed", requestIdentity);
			saveIdempotency(requestIdentity, requestHash, BalanceMutationType.CONFIRM, duplicate, reservationId, now);
			observability.recordLifecycleOutcome(BalanceOutcomeType.DUPLICATE);
			captureLifecycleAuditSafely(
				reservationId,
				requestIdentity,
				actorContext,
				BalanceMutationType.CONFIRM,
				now,
				"CONFIRM_DUPLICATE",
				ledgerTransactionId
			);
			return new ReservationLifecycleResult(duplicate, reservationId);
		}
		BalanceState state = loadStateForUpdate(reservation);
		balanceStateRepository.save(state.confirm(reservation.direction(), reservation.amount(), now).toRecord());
		reservationRepository.save(reservation.confirm(ledgerTransactionId, actorContext.correlationId(), now).toRecord());

		BalanceOutcome accepted = BalanceOutcome.accepted("BALANCE_RESERVATION_CONFIRMED", "Reservation confirmed", requestIdentity);
		saveIdempotency(requestIdentity, requestHash, BalanceMutationType.CONFIRM, accepted, reservationId, now);
		observability.recordLifecycleOutcome(BalanceOutcomeType.ACCEPTED);
		captureLifecycleAuditSafely(
			reservationId,
			requestIdentity,
			actorContext,
			BalanceMutationType.CONFIRM,
			now,
			"CONFIRM_ACCEPTED",
			ledgerTransactionId
		);
		return new ReservationLifecycleResult(accepted, reservationId);
	}

	private ReservationReleaseResult doRelease(UUID reservationId, RequestIdentity requestIdentity, ReleaseReason releaseReason, ActorContext actorContext) {
		Instant now = Instant.now(clock);
		RequestHash requestHash = new RequestHash(reservationId + "|" + releaseReason);
		Optional<IdempotencyDecision> decision = idempotencyService.evaluate(
			requestIdentity,
			requestHash,
			BalanceMutationType.RELEASE,
			"BALANCE_RESERVATION_RELEASE_DUPLICATE",
			"Duplicate release request",
			"Request identity already used for a different release intent",
			now);
		if (decision.isPresent()) {
			IdempotencyDecision evaluated = decision.orElseThrow();
			observability.recordLifecycleOutcome(evaluated.outcome().outcome());
			observability.recordDuplicate();
			captureLifecycleAuditSafely(
				reservationId,
				requestIdentity,
				actorContext,
				BalanceMutationType.RELEASE,
				now,
				"RELEASE_DUPLICATE",
				null
			);
			return new ReservationReleaseResult(evaluated.outcome(), reservationId);
		}

		FundsReservation reservation = reservationRepository.findByIdForUpdate(reservationId)
			.map(FundsReservation::fromRecord)
			.orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
		if (reservation.status() != ReservationStatus.ACTIVE && reservation.status() != ReservationStatus.RECOVERY_PENDING) {
			var duplicate = BalanceOutcome.duplicate("BALANCE_RESERVATION_ALREADY_RELEASED", "Reservation already finalized", requestIdentity);
			saveIdempotency(requestIdentity, requestHash, BalanceMutationType.RELEASE, duplicate, reservationId, now);
			observability.recordLifecycleOutcome(BalanceOutcomeType.DUPLICATE);
			captureLifecycleAuditSafely(
				reservationId,
				requestIdentity,
				actorContext,
				BalanceMutationType.RELEASE,
				now,
				"RELEASE_DUPLICATE",
				null
			);
			return new ReservationReleaseResult(duplicate, reservationId);
		}
		BalanceState state = loadStateForUpdate(reservation);
		balanceStateRepository.save(state.release(reservation.direction(), reservation.amount(), now).toRecord());
		ReservationStatus targetStatus = switch (releaseReason) {
			case EXPIRED -> ReservationStatus.EXPIRED;
			case RECOVERY -> ReservationStatus.RECOVERY_PENDING;
			case CANCELLED, FAILED_WORKFLOW -> ReservationStatus.CANCELLED;
		};
		reservationRepository.save(reservation.release(targetStatus, now).toRecord());

		BalanceOutcome accepted = BalanceOutcome.accepted("BALANCE_RESERVATION_RELEASED", "Reservation released", requestIdentity);
		saveIdempotency(requestIdentity, requestHash, BalanceMutationType.RELEASE, accepted, reservationId, now);
		observability.recordLifecycleOutcome(BalanceOutcomeType.ACCEPTED);
		captureLifecycleAuditSafely(
			reservationId,
			requestIdentity,
			actorContext,
			BalanceMutationType.RELEASE,
			now,
			"RELEASE_ACCEPTED",
			null
		);
		return new ReservationReleaseResult(accepted, reservationId);
	}

	private BalanceState loadStateForUpdate(FundsReservation reservation) {
		return balanceStateRepository.findByAccountAndCurrencyForUpdate(reservation.accountId(), reservation.currency())
			.map(BalanceState::fromRecord)
			.orElseThrow(() -> new IllegalArgumentException("Balance state not found for reservation"));
	}

	private void saveIdempotency(RequestIdentity requestIdentity, RequestHash requestHash, BalanceMutationType mutationType, BalanceOutcome outcome,
			UUID reservationId, Instant now) {
		idempotencyRepository.save(new BalanceIdempotencyRepositoryPort.IdempotencyRecord(
			requestIdentity,
			requestHash,
			mutationType,
			outcome.outcome().name(),
			outcome.code(),
			"\"" + reservationId + "\"",
			now,
			now));
	}

	private void captureLifecycleAuditSafely(
			UUID reservationId,
			RequestIdentity requestIdentity,
			ActorContext actorContext,
			BalanceMutationType mutationType,
			Instant occurredAt,
			String stateTo,
			String ledgerTransactionId
	) {
		try {
			auditCaptureUseCase.capture(auditTranslator.toLifecycleCaptureCommand(
				reservationId,
				requestIdentity,
				actorContext,
				mutationType,
				occurredAt,
				stateTo,
				ledgerTransactionId,
				null,
				requestIdentity.requestId()
			));
		} catch (RuntimeException ex) {
			log.warn("Shared audit capture failed for balance lifecycle reservationId={}", reservationId, ex);
		}
	}
}
