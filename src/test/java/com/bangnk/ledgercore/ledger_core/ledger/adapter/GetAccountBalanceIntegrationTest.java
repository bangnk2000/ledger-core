package com.bangnk.ledgercore.ledger_core.ledger.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerEntryJpaRepository;
import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerJpaEntities.LedgerEntryJpaEntity;
import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerJpaEntities.LedgerTransactionJpaEntity;
import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerTransactionJpaRepository;
import com.bangnk.ledgercore.ledger_core.ledger.application.query.GetAccountBalanceService;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.TransactionStatus;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GetAccountBalanceIntegrationTest extends PostgresIntegrationTestBase {

	private static final int ACCOUNT_ENTRY_COUNT = 10_000;
	private static final int BATCH_SIZE = 500;

	@Autowired
	private GetAccountBalanceService balanceService;

	@Autowired
	private LedgerTransactionJpaRepository transactionRepository;

	@Autowired
	private LedgerEntryJpaRepository entryRepository;

	@Test
	void calculatesBalanceAcrossAtLeastTenThousandPostedEntriesWithinTwoSeconds() {
		seedPostedEntries();
		seedRejectedNoise();

		long startedAt = System.nanoTime();
		var balance = balanceService.getBalance(new AccountId("bulk-cash"), "USD");
		Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);

		assertThat(balance.accountId()).isEqualTo("bulk-cash");
		assertThat(balance.balance()).isEqualByComparingTo("30000.0000");
		assertThat(balance.entryCount()).isEqualTo(ACCOUNT_ENTRY_COUNT);
		assertThat(elapsed).isLessThan(Duration.ofSeconds(2));
	}

	private void seedPostedEntries() {
		Instant now = Instant.parse("2026-05-12T00:00:00Z");
		List<LedgerTransactionJpaEntity> transactions = new ArrayList<>(BATCH_SIZE);
		List<LedgerEntryJpaEntity> entries = new ArrayList<>(BATCH_SIZE * 2);
		for (int i = 0; i < ACCOUNT_ENTRY_COUNT; i++) {
			UUID transactionId = UUID.randomUUID();
			boolean debit = i % 2 == 0;
			BigDecimal amount = new BigDecimal(debit ? "10.0000" : "4.0000");
			Direction targetDirection = debit ? Direction.DEBIT : Direction.CREDIT;
			Direction offsetDirection = debit ? Direction.CREDIT : Direction.DEBIT;
			transactions.add(new LedgerTransactionJpaEntity(
				transactionId,
				"balance-integration",
				"request-" + i,
				"hash-" + i,
				TransactionStatus.POSTED,
				"bulk-balance",
				"seed transaction",
				"{}",
				"corr-" + i,
				null,
				"seed",
				ActorType.SYSTEM,
				now,
				now,
				null,
				null,
				now));
			entries.add(entry(transactionId, "bulk-cash-" + i, "bulk-cash", targetDirection, amount, now));
			entries.add(entry(transactionId, "bulk-offset-" + i, "bulk-offset", offsetDirection, amount, now));
			if ((i + 1) % BATCH_SIZE == 0) {
				flushBatch(transactions, entries);
			}
		}
		flushBatch(transactions, entries);
	}

	private void seedRejectedNoise() {
		Instant now = Instant.parse("2026-05-12T00:05:00Z");
		UUID transactionId = UUID.randomUUID();
		transactionRepository.save(new LedgerTransactionJpaEntity(
			transactionId,
			"balance-integration",
			"request-rejected",
			"hash-rejected",
			TransactionStatus.REJECTED,
			"bulk-balance-rejected",
			"noise transaction",
			"{}",
			"corr-rejected",
			null,
			"seed",
			ActorType.SYSTEM,
			now,
			null,
			"LEDGER_UNBALANCED",
			"noise",
			now));
		entryRepository.saveAll(List.of(
			entry(transactionId, "rejected-target", "bulk-cash", Direction.DEBIT, new BigDecimal("9999.0000"), now),
			entry(transactionId, "rejected-offset", "bulk-noise", Direction.CREDIT, new BigDecimal("9999.0000"), now)));
	}

	private void flushBatch(List<LedgerTransactionJpaEntity> transactions, List<LedgerEntryJpaEntity> entries) {
		if (transactions.isEmpty()) {
			return;
		}
		transactionRepository.saveAll(transactions);
		entryRepository.saveAll(entries);
		transactionRepository.flush();
		entryRepository.flush();
		transactions.clear();
		entries.clear();
	}

	private static LedgerEntryJpaEntity entry(
		UUID transactionId,
		String lineId,
		String accountId,
		Direction direction,
		BigDecimal amount,
		Instant postedAt
	) {
		return new LedgerEntryJpaEntity(
			UUID.randomUUID(),
			transactionId,
			lineId,
			accountId,
			direction,
			amount,
			"USD",
			"{}",
			postedAt,
			postedAt);
	}
}
