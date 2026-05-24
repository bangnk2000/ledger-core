package com.bangnk.ledgercore.ledger_core.ledger.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bangnk.ledgercore.ledger_core.ledger.application.PostingOutcome.LedgerDomainException;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction.LedgerEntryDraft;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerTransactionId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LineId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.Money;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DoubleEntryInvariantsTest {

	@Test
	void preservesBalancedTotalsAcrossEntries() {
		LedgerTransaction transaction = LedgerTransaction.post(
			LedgerTransactionId.newId(),
			"inv-invariant-1",
			"invariant posting",
			Map.of("channel", "test"),
			trace(),
			List.of(
				draft("line-1", "cash", Direction.DEBIT, "100.0000"),
				draft("line-2", "receivable", Direction.DEBIT, "10.0000"),
				draft("line-3", "revenue", Direction.CREDIT, "110.0000")),
			Instant.parse("2026-05-19T00:00:00Z"));

		BigDecimal debitTotal = transaction.entries().stream()
			.filter(entry -> entry.direction() == Direction.DEBIT)
			.map(entry -> entry.money().amount())
			.reduce(BigDecimal.ZERO.setScale(4), BigDecimal::add);
		BigDecimal creditTotal = transaction.entries().stream()
			.filter(entry -> entry.direction() == Direction.CREDIT)
			.map(entry -> entry.money().amount())
			.reduce(BigDecimal.ZERO.setScale(4), BigDecimal::add);

		assertEquals(0, debitTotal.compareTo(creditTotal));
	}

	@Test
	void rejectsCrossCurrencyEntries() {
		LedgerDomainException ex = assertThrows(LedgerDomainException.class, () -> LedgerTransaction.post(
			LedgerTransactionId.newId(),
			"inv-invariant-2",
			"currency mismatch",
			null,
			trace(),
			List.of(
				new LedgerEntryDraft(new LineId("line-1"), new AccountId("cash"), Direction.DEBIT, new Money(new BigDecimal("100.0000"), "USD"), null),
				new LedgerEntryDraft(new LineId("line-2"), new AccountId("revenue"), Direction.CREDIT, new Money(new BigDecimal("100.0000"), "EUR"), null)),
			Instant.now()));

		assertEquals("LEDGER_CURRENCY_MISMATCH", ex.getCode());
	}

	@Test
	void keepsTransactionAndEntryMetadataImmutable() {
		LedgerTransaction transaction = LedgerTransaction.post(
			LedgerTransactionId.newId(),
			"inv-invariant-3",
			"immutability",
			new HashMap<>(Map.of("source", "api")),
			trace(),
			List.of(
				new LedgerEntryDraft(
					new LineId("line-1"),
					new AccountId("cash"),
					Direction.DEBIT,
					new Money(new BigDecimal("50.0000"), "USD"),
					new HashMap<>(Map.of("traceId", "corr-1"))),
				draft("line-2", "revenue", Direction.CREDIT, "50.0000")),
			Instant.parse("2026-05-19T00:00:00Z"));

		assertThrows(UnsupportedOperationException.class, () -> transaction.metadata().put("source", "manual"));
		assertThrows(UnsupportedOperationException.class, () -> transaction.entries().add(transaction.entries().getFirst()));
		assertThrows(UnsupportedOperationException.class, () -> transaction.entries().getFirst().metadata().put("traceId", "corr-2"));
	}

	private static LedgerEntryDraft draft(String lineId, String accountId, Direction direction, String amount) {
		return new LedgerEntryDraft(
			new LineId(lineId),
			new AccountId(accountId),
			direction,
			new Money(new BigDecimal(amount), "USD"),
			null);
	}

	private static AuditTrace trace() {
		return new AuditTrace(
			new RequestIdentity("test", "req-invariant"),
			"corr-invariant",
			null,
			new AuditTrace.Actor("tester", ActorType.SYSTEM),
			Instant.parse("2026-05-19T00:00:00Z"));
	}
}
