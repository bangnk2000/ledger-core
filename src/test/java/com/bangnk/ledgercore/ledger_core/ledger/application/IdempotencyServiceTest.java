package com.bangnk.ledgercore.ledger_core.ledger.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bangnk.ledgercore.ledger_core.ledger.application.command.IdempotencyService;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.IdempotencyRecordRepository;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.PostingOutcomeStore;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.PostingOutcomeType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity.RequestHash;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-12T00:00:00Z"), ZoneOffset.UTC);

	@Mock
	private IdempotencyRecordRepository repository;

	@Mock
	private PostingOutcomeStore postingOutcomeStore;

	@Test
	void returnsStoredOutcomeWhenRequestHashMatches() {
		IdempotencyService service = new IdempotencyService(repository, postingOutcomeStore, CLOCK);
		RequestIdentity requestIdentity = new RequestIdentity("scope-a", "request-1");
		RequestHash requestHash = new RequestHash("hash-1");
		AuditTrace trace = trace(requestIdentity);
		IdempotencyRecord record = new IdempotencyRecord(
			requestIdentity,
			requestHash,
			PostingOutcomeType.ACCEPTED,
			"transaction-1",
			"LEDGER_POSTED",
			Instant.parse("2026-05-11T23:59:00Z"),
			Instant.parse("2026-05-11T23:59:00Z"));
		PostingOutcome stored = PostingOutcome.duplicate(
			"LEDGER_POSTED",
			"Duplicate request resolved to stored accepted outcome",
			requestIdentity,
			"transaction-1",
			Instant.parse("2026-05-11T23:59:30Z"),
			trace);

		when(repository.findByIdentity(requestIdentity)).thenReturn(Optional.of(record));
		when(postingOutcomeStore.findStoredOutcome(record)).thenReturn(Optional.of(stored));

		Optional<PostingOutcome> resolved = service.resolveExisting(requestIdentity, requestHash, trace);

		assertThat(resolved).contains(stored);
		verify(repository).touch(record.touch(Instant.now(CLOCK)));
	}

	@Test
	void returnsConflictWhenRequestHashDiffers() {
		IdempotencyService service = new IdempotencyService(repository, postingOutcomeStore, CLOCK);
		RequestIdentity requestIdentity = new RequestIdentity("scope-a", "request-2");
		RequestHash storedHash = new RequestHash("hash-stored");
		RequestHash retryHash = new RequestHash("hash-retry");
		AuditTrace trace = trace(requestIdentity);
		IdempotencyRecord record = new IdempotencyRecord(
			requestIdentity,
			storedHash,
			PostingOutcomeType.ACCEPTED,
			"transaction-2",
			"LEDGER_POSTED",
			Instant.parse("2026-05-11T23:59:00Z"),
			Instant.parse("2026-05-11T23:59:00Z"));

		when(repository.findByIdentity(requestIdentity)).thenReturn(Optional.of(record));

		Optional<PostingOutcome> resolved = service.resolveExisting(requestIdentity, retryHash, trace);

		assertThat(resolved).isPresent();
		assertThat(resolved.orElseThrow().outcome()).isEqualTo(PostingOutcomeType.CONFLICT);
		assertThat(resolved.orElseThrow().code()).isEqualTo("LEDGER_IDEMPOTENCY_CONFLICT");
		verify(repository).touch(record.touch(Instant.now(CLOCK)));
		verify(postingOutcomeStore, never()).findStoredOutcome(any());
	}

	@Test
	void storesStableOutcomeMetadataForLaterDuplicateResolution() {
		IdempotencyService service = new IdempotencyService(repository, postingOutcomeStore, CLOCK);
		RequestIdentity requestIdentity = new RequestIdentity("scope-a", "request-3");
		RequestHash requestHash = new RequestHash("hash-3");
		PostingOutcome outcome = PostingOutcome.rejected(
			"LEDGER_UNBALANCED",
			"Debit and credit totals must match",
			requestIdentity,
			"transaction-3",
			trace(requestIdentity));

		service.store(requestIdentity, requestHash, outcome);

		verify(repository).save(new IdempotencyRecord(
			requestIdentity,
			requestHash,
			PostingOutcomeType.REJECTED,
			"transaction-3",
			"LEDGER_UNBALANCED",
			Instant.now(CLOCK),
			Instant.now(CLOCK)));
	}

	private static AuditTrace trace(RequestIdentity requestIdentity) {
		return new AuditTrace(
			requestIdentity,
			"corr-1",
			null,
			new AuditTrace.Actor("tester", ActorType.SYSTEM),
			Instant.parse("2026-05-12T00:00:00Z"));
	}
}
