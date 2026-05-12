package com.bangnk.ledgercore.ledger_core.ledger.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bangnk.ledgercore.ledger_core.ledger.application.command.IdempotencyService;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionCommand;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionService;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostingRequestHasher;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.AuditEventPublisher;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.BalanceSnapshot;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.LedgerEntryRepository;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.LedgerTransactionRepository;
import com.bangnk.ledgercore.ledger_core.ledger.application.query.GetAccountBalanceService;
import com.bangnk.ledgercore.ledger_core.ledger.config.LedgerObservabilityConfig.LedgerMetrics;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction.LedgerEntryDraft;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.PostingOutcomeType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LineId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.Money;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditEventPublisherTest {

	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-12T00:00:00Z"), ZoneOffset.UTC);

	@Mock
	private LedgerTransactionRepository transactionRepository;

	@Mock
	private LedgerEntryRepository entryRepository;

	@Mock
	private IdempotencyService idempotencyService;

	@Mock
	private AuditEventPublisher auditEventPublisher;

	private SimpleMeterRegistry meterRegistry;
	private PostingRequestHasher requestHasher;

	@BeforeEach
	void setUp() {
		meterRegistry = new SimpleMeterRegistry();
		requestHasher = new PostingRequestHasher(new ObjectMapper().registerModule(new JavaTimeModule()));
	}

	@Test
	void publishesAcceptedEventForSuccessfulPosting() {
		PostLedgerTransactionService service = postingService();
		PostLedgerTransactionCommand command = validCommand("accepted-request");
		when(idempotencyService.resolveExisting(command.requestIdentity(), requestHasher.hash(command), command.auditTrace()))
			.thenReturn(Optional.empty());

		PostingOutcome outcome = service.post(command);

		assertThat(outcome.outcome()).isEqualTo(PostingOutcomeType.ACCEPTED);
		verify(auditEventPublisher).publish(argThat(event ->
			event.eventType().equals("POSTING_ACCEPTED")
				&& event.transactionId().equals(outcome.transactionId())
				&& event.safeDetails().get("entries").equals(2)));
		assertThat(meterRegistry.get("ledger.postings").tag("outcome", "accepted").counter().count()).isEqualTo(1.0);
	}

	@Test
	void publishesRejectedEventForDomainRejection() {
		PostLedgerTransactionService service = postingService();
		PostLedgerTransactionCommand command = unbalancedCommand("rejected-request");
		when(idempotencyService.resolveExisting(command.requestIdentity(), requestHasher.hash(command), command.auditTrace()))
			.thenReturn(Optional.empty());

		PostingOutcome outcome = service.post(command);

		assertThat(outcome.outcome()).isEqualTo(PostingOutcomeType.REJECTED);
		verify(auditEventPublisher).publish(argThat(event ->
			event.eventType().equals("POSTING_REJECTED")
				&& event.safeDetails().get("code").equals("LEDGER_UNBALANCED")));
		assertThat(meterRegistry.get("ledger.postings").tag("outcome", "rejected").counter().count()).isEqualTo(1.0);
	}

	@Test
	void publishesDuplicateEventForStoredDuplicateOutcome() {
		PostLedgerTransactionService service = postingService();
		PostLedgerTransactionCommand command = validCommand("duplicate-request");
		PostingOutcome duplicate = PostingOutcome.duplicate(
			"LEDGER_POSTED",
			"Duplicate request resolved to stored accepted outcome",
			command.requestIdentity(),
			"transaction-duplicate",
			Instant.parse("2026-05-11T23:59:00Z"),
			command.auditTrace());
		when(idempotencyService.resolveExisting(command.requestIdentity(), requestHasher.hash(command), command.auditTrace()))
			.thenReturn(Optional.of(duplicate));

		PostingOutcome outcome = service.post(command);

		assertThat(outcome.outcome()).isEqualTo(PostingOutcomeType.DUPLICATE);
		verify(auditEventPublisher).publish(argThat(event ->
			event.eventType().equals("DUPLICATE_REQUEST")
				&& event.safeDetails().get("code").equals("LEDGER_POSTED")));
		assertThat(meterRegistry.get("ledger.postings").tag("outcome", "duplicate").counter().count()).isEqualTo(1.0);
	}

	@Test
	void publishesConflictEventForIdempotencyConflict() {
		PostLedgerTransactionService service = postingService();
		PostLedgerTransactionCommand command = validCommand("conflict-request");
		PostingOutcome conflict = PostingOutcome.conflict(
			"LEDGER_IDEMPOTENCY_CONFLICT",
			"Idempotency key was reused with different request content",
			command.requestIdentity(),
			command.auditTrace());
		when(idempotencyService.resolveExisting(command.requestIdentity(), requestHasher.hash(command), command.auditTrace()))
			.thenReturn(Optional.of(conflict));

		PostingOutcome outcome = service.post(command);

		assertThat(outcome.outcome()).isEqualTo(PostingOutcomeType.CONFLICT);
		verify(auditEventPublisher).publish(argThat(event ->
			event.eventType().equals("CONFLICTING_REQUEST")
				&& event.safeDetails().get("code").equals("LEDGER_IDEMPOTENCY_CONFLICT")));
		assertThat(meterRegistry.get("ledger.postings").tag("outcome", "conflict").counter().count()).isEqualTo(1.0);
	}

	@Test
	void publishesFailedEventForUnexpectedPersistenceFailure() {
		PostLedgerTransactionService service = postingService();
		PostLedgerTransactionCommand command = validCommand("failed-request");
		when(idempotencyService.resolveExisting(command.requestIdentity(), requestHasher.hash(command), command.auditTrace()))
			.thenReturn(Optional.empty());
		when(transactionRepository.save(org.mockito.ArgumentMatchers.any())).thenThrow(new IllegalStateException("database unavailable"));

		assertThatThrownBy(() -> service.post(command))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("database unavailable");

		verify(auditEventPublisher).publish(argThat(event ->
			event.eventType().equals("POSTING_FAILED")
				&& event.safeDetails().get("code").equals("LEDGER_POSTING_FAILED")));
		assertThat(meterRegistry.get("ledger.postings").tag("outcome", "failed").counter().count()).isEqualTo(1.0);
	}

	@Test
	void publishesBalanceCalculatedEvent() {
		GetAccountBalanceService service = new GetAccountBalanceService(
			entryRepository,
			auditEventPublisher,
			new LedgerMetrics(meterRegistry),
			CLOCK);
		when(entryRepository.summarizePostedBalance(new AccountId("cash"), "USD"))
			.thenReturn(new BalanceSnapshot(new BigDecimal("110.0000"), new BigDecimal("40.0000"), 3L));

		var balance = service.getBalance(new AccountId("cash"), "USD");

		assertThat(balance.balance()).isEqualByComparingTo("70.0000");
		verify(auditEventPublisher).publish(argThat(event ->
			event.eventType().equals("BALANCE_CALCULATED")
				&& event.safeDetails().get("currency").equals("USD")
				&& event.safeDetails().get("entryCount").equals(3L)));
		assertThat(meterRegistry.get("ledger.balance.requests").tag("outcome", "success").counter().count()).isEqualTo(1.0);
	}

	private PostLedgerTransactionService postingService() {
		return new PostLedgerTransactionService(
			transactionRepository,
			entryRepository,
			requestHasher,
			idempotencyService,
			auditEventPublisher,
			new LedgerMetrics(meterRegistry),
			CLOCK);
	}

	private static PostLedgerTransactionCommand validCommand(String requestId) {
		RequestIdentity identity = new RequestIdentity("audit-test", requestId);
		return new PostLedgerTransactionCommand(
			identity,
			trace(identity),
			"business-" + requestId,
			"audit test",
			Map.of("channel", "api"),
			List.of(
				draft("debit-" + requestId, "cash", Direction.DEBIT, "100.0000"),
				draft("credit-" + requestId, "revenue", Direction.CREDIT, "100.0000")));
	}

	private static PostLedgerTransactionCommand unbalancedCommand(String requestId) {
		RequestIdentity identity = new RequestIdentity("audit-test", requestId);
		return new PostLedgerTransactionCommand(
			identity,
			trace(identity),
			"business-" + requestId,
			"audit test",
			Map.of("channel", "api"),
			List.of(
				draft("debit-" + requestId, "cash", Direction.DEBIT, "100.0000"),
				draft("credit-" + requestId, "revenue", Direction.CREDIT, "90.0000")));
	}

	private static AuditTrace trace(RequestIdentity identity) {
		return new AuditTrace(
			identity,
			"corr-" + identity.requestId(),
			"cause-" + identity.requestId(),
			new AuditTrace.Actor("audit-tester", ActorType.SYSTEM),
			Instant.parse("2026-05-12T00:00:00Z"));
	}

	private static LedgerEntryDraft draft(String lineId, String accountId, Direction direction, String amount) {
		return new LedgerEntryDraft(
			new LineId(lineId),
			new AccountId(accountId),
			direction,
			new Money(new BigDecimal(amount), "USD"),
			Map.of("lineRole", direction.name().toLowerCase()));
	}
}
