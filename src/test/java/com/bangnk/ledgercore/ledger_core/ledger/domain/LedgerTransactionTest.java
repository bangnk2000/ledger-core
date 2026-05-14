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
import java.util.List;
import org.junit.jupiter.api.Test;

class LedgerTransactionTest {

	@Test
	void createsBalancedTransaction() {
		LedgerTransaction transaction = LedgerTransaction.post(
			LedgerTransactionId.newId(),
			"invoice-1",
			"balanced",
			null,
			trace(),
			List.of(
				draft("debit-1", "cash", Direction.DEBIT, "100.0000"),
				draft("credit-1", "revenue", Direction.CREDIT, "100.0000")),
			Instant.parse("2026-05-12T00:00:00Z"));

		assertEquals(2, transaction.entries().size());
		assertEquals("cash", transaction.entries().get(0).accountId().value());
	}

	@Test
	void rejectsUnbalancedTotals() {
		LedgerDomainException ex = assertThrows(LedgerDomainException.class, () -> LedgerTransaction.post(
			LedgerTransactionId.newId(),
			null,
			null,
			null,
			trace(),
			List.of(
				draft("debit-1", "cash", Direction.DEBIT, "100.0000"),
				draft("credit-1", "revenue", Direction.CREDIT, "99.0000")),
			Instant.now()));

		assertEquals("LEDGER_UNBALANCED", ex.getCode());
	}

	@Test
	void rejectsDuplicateLineIds() {
		LedgerDomainException ex = assertThrows(LedgerDomainException.class, () -> LedgerTransaction.post(
			LedgerTransactionId.newId(),
			null,
			null,
			null,
			trace(),
			List.of(
				draft("line-1", "cash", Direction.DEBIT, "100.0000"),
				draft("line-1", "revenue", Direction.CREDIT, "100.0000")),
			Instant.now()));

		assertEquals("LEDGER_DUPLICATE_LINE_ID", ex.getCode());
	}

	@Test
	void rejectsMissingCreditDirection() {
		LedgerDomainException ex = assertThrows(LedgerDomainException.class, () -> LedgerTransaction.post(
			LedgerTransactionId.newId(),
			null,
			null,
			null,
			trace(),
			List.of(
				draft("line-1", "cash", Direction.DEBIT, "100.0000"),
				draft("line-2", "cash", Direction.DEBIT, "100.0000")),
			Instant.now()));

		assertEquals("LEDGER_MISSING_DIRECTION", ex.getCode());
	}

	@Test
	void rejectsInvalidAmount() {
		assertThrows(IllegalArgumentException.class, () -> draft("line-1", "cash", Direction.DEBIT, "0.0000"));
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
			new RequestIdentity("test", "req-1"),
			"corr-1",
			null,
			new AuditTrace.Actor("tester", ActorType.SYSTEM),
			Instant.parse("2026-05-12T00:00:00Z"));
	}
}
