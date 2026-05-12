package com.bangnk.ledgercore.ledger_core.ledger.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.JpaLedgerPersistenceAdapter;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionCommand;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionService;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction.LedgerEntryDraft;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LineId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.Money;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PostLedgerTransactionIntegrationTest extends PostgresIntegrationTestBase {

	@Autowired
	private PostLedgerTransactionService service;

	@Autowired
	private JpaLedgerPersistenceAdapter persistenceAdapter;

	@Test
	void persistsBalancedPostingAtomically() {
		var outcome = service.post(new PostLedgerTransactionCommand(
			new RequestIdentity("integration-test", "req-1"),
			new AuditTrace(
				new RequestIdentity("integration-test", "req-1"),
				"corr-1",
				null,
				new AuditTrace.Actor("integration", ActorType.SYSTEM),
				Instant.parse("2026-05-12T00:00:00Z")),
			"invoice-1",
			"balanced",
			null,
			List.of(
				draft("line-1", "cash", Direction.DEBIT, "100.0000"),
				draft("line-2", "revenue", Direction.CREDIT, "100.0000"))));

		assertEquals("LEDGER_POSTED", outcome.code());
		assertEquals(2, persistenceAdapter.findEntriesByTransactionId(new com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerTransactionId(java.util.UUID.fromString(outcome.transactionId()))).size());
	}

	@Test
	void rejectsUnbalancedPostingWithoutPersistingEntries() {
		var outcome = service.post(new PostLedgerTransactionCommand(
			new RequestIdentity("integration-test", "req-2"),
			new AuditTrace(
				new RequestIdentity("integration-test", "req-2"),
				"corr-2",
				null,
				new AuditTrace.Actor("integration", ActorType.SYSTEM),
				Instant.parse("2026-05-12T00:00:00Z")),
			"invoice-2",
			"unbalanced",
			null,
			List.of(
				draft("line-1", "cash", Direction.DEBIT, "100.0000"),
				draft("line-2", "revenue", Direction.CREDIT, "50.0000"))));

		assertEquals("LEDGER_UNBALANCED", outcome.code());
		assertEquals(0, persistenceAdapter.findEntriesByTransactionId(new com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerTransactionId(java.util.UUID.fromString(outcome.transactionId()))).size());
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
