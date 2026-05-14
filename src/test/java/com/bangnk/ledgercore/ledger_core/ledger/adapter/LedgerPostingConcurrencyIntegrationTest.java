package com.bangnk.ledgercore.ledger_core.ledger.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.IdempotencyRecordJpaRepository;
import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.JpaLedgerPersistenceAdapter;
import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerEntryJpaRepository;
import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerTransactionJpaRepository;
import com.bangnk.ledgercore.ledger_core.ledger.application.PostingOutcome;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionCommand;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionService;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction.LedgerEntryDraft;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.PostingOutcomeType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LineId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerTransactionId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.Money;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class LedgerPostingConcurrencyIntegrationTest extends PostgresIntegrationTestBase {

	private static final int CONCURRENT_REQUESTS = 100;

	@Autowired
	private PostLedgerTransactionService service;

	@Autowired
	private JpaLedgerPersistenceAdapter persistenceAdapter;

	@Autowired
	private LedgerTransactionJpaRepository transactionRepository;

	@Autowired
	private LedgerEntryJpaRepository entryRepository;

	@Autowired
	private IdempotencyRecordJpaRepository idempotencyRepository;

	@Test
	void acceptsHundredConcurrentValidPostingsWithoutPartialRecords() throws Exception {
		long transactionsBefore = transactionRepository.count();
		long entriesBefore = entryRepository.count();
		long idempotencyBefore = idempotencyRepository.count();
		ExecutorService executor = Executors.newFixedThreadPool(16);
		try {
			List<Callable<PostingOutcome>> tasks = new ArrayList<>();
			for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
				final int index = i;
				tasks.add(() -> service.post(command(index)));
			}

			List<Future<PostingOutcome>> futures = executor.invokeAll(tasks);
			List<PostingOutcome> outcomes = collect(futures);

			assertThat(outcomes)
				.hasSize(CONCURRENT_REQUESTS)
				.allSatisfy(outcome -> {
					assertThat(outcome.outcome()).isEqualTo(PostingOutcomeType.ACCEPTED);
					assertThat(outcome.code()).isEqualTo("LEDGER_POSTED");
				});

			assertThat(transactionRepository.count() - transactionsBefore).isEqualTo(CONCURRENT_REQUESTS);
			assertThat(entryRepository.count() - entriesBefore).isEqualTo(CONCURRENT_REQUESTS * 2L);
			assertThat(idempotencyRepository.count() - idempotencyBefore).isEqualTo(CONCURRENT_REQUESTS);

			for (PostingOutcome outcome : outcomes) {
				List<?> entries = persistenceAdapter.findEntriesByTransactionId(
					new LedgerTransactionId(UUID.fromString(outcome.transactionId())));
				assertThat(entries).hasSize(2);
			}
		} finally {
			executor.shutdownNow();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		}
	}

	private static List<PostingOutcome> collect(List<Future<PostingOutcome>> futures) throws InterruptedException, ExecutionException {
		List<PostingOutcome> outcomes = new ArrayList<>(futures.size());
		for (Future<PostingOutcome> future : futures) {
			outcomes.add(future.get());
		}
		return outcomes;
	}

	private static PostLedgerTransactionCommand command(int index) {
		String requestId = "concurrency-request-" + index;
		RequestIdentity identity = new RequestIdentity("concurrency-test", requestId);
		return new PostLedgerTransactionCommand(
			identity,
			new AuditTrace(
				identity,
				"corr-" + index,
				null,
				new AuditTrace.Actor("integration", ActorType.SYSTEM),
				Instant.parse("2026-05-12T00:00:00Z")),
			"batch-" + index,
			"concurrent posting " + index,
			null,
			List.of(
				draft("debit-" + index, "cash-" + index, Direction.DEBIT, "100.0000"),
				draft("credit-" + index, "revenue-" + index, Direction.CREDIT, "100.0000")));
	}

	private static LedgerEntryDraft draft(String lineId, String accountId, Direction direction, String amount) {
		return new LedgerEntryDraft(
			new LineId(lineId),
			new AccountId(accountId),
			direction,
			new Money(new BigDecimal(amount), "USD"),
			null);
	}
}
