package com.bangnk.ledgercore.ledger_core.ledger.balance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.command.BalanceMutationRequest;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.BalanceIdempotencyRepositoryPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.BalanceStateRepositoryPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.BalanceTransactionPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.FundsReservationRepositoryPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceActorType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.RequestIdentity;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BalanceMutationCharacterizationTest {

	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-19T00:00:00Z"), ZoneOffset.UTC);

	@Mock
	private BalanceStateRepositoryPort balanceStateRepository;
	@Mock
	private FundsReservationRepositoryPort reservationRepository;
	@Mock
	private BalanceIdempotencyRepositoryPort idempotencyRepository;
	@Mock
	private BalanceTransactionPort transactionPort;

	private ReserveFundsService service;

	@BeforeEach
	void setUp() {
		BalanceObservability observability = new BalanceObservability(new SimpleMeterRegistry());
		service = new ReserveFundsService(
			balanceStateRepository,
			reservationRepository,
			idempotencyRepository,
			transactionPort,
			new ProtectedWriteRetryExecutor(observability),
			new BalanceIdempotencyService(idempotencyRepository),
			new BalanceConsistencyGuard(),
			observability,
			CLOCK);
		when(transactionPort.withinProtectedWrite(any())).thenAnswer(invocation -> {
			@SuppressWarnings("unchecked")
			Supplier<Object> action = invocation.getArgument(0);
			return action.get();
		});
	}

	@Test
	void acceptsCreditReservationAndReturnsReservationId() {
		when(idempotencyRepository.find(any())).thenReturn(Optional.empty());
		when(balanceStateRepository.findByAccountAndCurrencyForUpdate(any(), any())).thenReturn(Optional.empty());

		var result = service.reserve(request("rq-accept", "acc-accept", "5.0000", BalanceDirection.CREDIT));

		assertThat(result.outcome().outcome().name()).isEqualTo("ACCEPTED");
		assertThat(result.outcome().code()).isEqualTo("BALANCE_RESERVED");
		assertThat(result.reservationId()).isNotNull();
		verify(balanceStateRepository).save(any());
		verify(reservationRepository).save(any());
	}

	@Test
	void rejectsDebitReservationWhenAvailableBalanceWouldGoNegative() {
		when(idempotencyRepository.find(any())).thenReturn(Optional.empty());
		when(balanceStateRepository.findByAccountAndCurrencyForUpdate(any(), any())).thenReturn(Optional.empty());

		var result = service.reserve(request("rq-reject", "acc-reject", "1.0000", BalanceDirection.DEBIT));

		assertThat(result.outcome().outcome().name()).isEqualTo("REJECTED");
		assertThat(result.outcome().code()).isEqualTo("BALANCE_INSUFFICIENT_FUNDS");
		assertThat(result.reservationId()).isNull();
		verify(reservationRepository, never()).save(any());
	}

	@Test
	void returnsDuplicateOutcomeAndPayloadReservationWhenSameIntentAlreadyRecorded() {
		RequestIdentity identity = new RequestIdentity("characterization", "rq-dup");
		UUID existingReservationId = UUID.randomUUID();
		when(idempotencyRepository.find(identity)).thenReturn(Optional.of(
			new BalanceIdempotencyRepositoryPort.IdempotencyRecord(
				identity,
				new RequestIdentity.RequestHash(identity.requestId() + "|5.0000"),
				BalanceMutationType.RESERVE,
				"ACCEPTED",
				"BALANCE_RESERVED",
				"\"" + existingReservationId + "\"",
				Instant.parse("2026-05-19T00:00:00Z"),
				Instant.parse("2026-05-19T00:00:00Z"))));

		var result = service.reserve(request(identity, "acc-dup", "5.0000", BalanceDirection.CREDIT));

		assertThat(result.outcome().outcome().name()).isEqualTo("DUPLICATE");
		assertThat(result.reservationId()).isEqualTo(existingReservationId);
		verify(balanceStateRepository, never()).save(any());
		verify(reservationRepository, never()).save(any());
	}

	@Test
	void returnsConflictOutcomeWhenSameRequestIdIsReusedWithDifferentAmount() {
		RequestIdentity identity = new RequestIdentity("characterization", "rq-conflict");
		when(idempotencyRepository.find(identity)).thenReturn(Optional.of(
			new BalanceIdempotencyRepositoryPort.IdempotencyRecord(
				identity,
				new RequestIdentity.RequestHash(identity.requestId() + "|1.0000"),
				BalanceMutationType.RESERVE,
				"ACCEPTED",
				"BALANCE_RESERVED",
				null,
				Instant.parse("2026-05-19T00:00:00Z"),
				Instant.parse("2026-05-19T00:00:00Z"))));

		var result = service.reserve(request(identity, "acc-conflict", "5.0000", BalanceDirection.CREDIT));

		assertThat(result.outcome().outcome().name()).isEqualTo("CONFLICT");
		assertThat(result.outcome().code()).isEqualTo("BALANCE_IDEMPOTENCY_CONFLICT");
		assertThat(result.reservationId()).isNull();
		verify(balanceStateRepository, never()).save(any());
		verify(reservationRepository, never()).save(any());
	}

	private static BalanceMutationRequest request(String requestId, String accountId, String amount, BalanceDirection direction) {
		return request(new RequestIdentity("characterization", requestId), accountId, amount, direction);
	}

	private static BalanceMutationRequest request(
			RequestIdentity requestIdentity,
			String accountId,
			String amount,
			BalanceDirection direction
	) {
		return new BalanceMutationRequest(
			requestIdentity,
			BalanceMutationType.RESERVE,
			List.of(new AccountId(accountId)),
			new CurrencyCode("USD"),
			new MoneyAmount(new BigDecimal(amount)),
			direction,
			new ActorContext("test-user", BalanceActorType.SYSTEM, "corr-" + requestIdentity.requestId(), null),
			"order-" + requestIdentity.requestId(),
			Instant.parse("2026-05-20T12:00:00Z"));
	}
}
