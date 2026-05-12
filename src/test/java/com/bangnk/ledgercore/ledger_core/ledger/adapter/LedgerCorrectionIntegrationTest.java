package com.bangnk.ledgercore.ledger_core.ledger.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.JpaLedgerPersistenceAdapter;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.LedgerCorrectionFactory;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionCommand;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionService;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerEntry;
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
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class LedgerCorrectionIntegrationTest extends PostgresIntegrationTestBase {

	@Autowired
	private PostLedgerTransactionService service;

	@Autowired
	private JpaLedgerPersistenceAdapter persistenceAdapter;

	@Autowired
	private LedgerCorrectionFactory correctionFactory;

	@Test
	void correctionCreatesNewEntriesWithoutMutatingOriginalOnes() {
		String originalTransactionId = postBalanced("original-request").transactionId();
		List<LedgerEntry> originalEntries = persistenceAdapter.findEntriesByTransactionId(new LedgerTransactionId(UUID.fromString(originalTransactionId)));

		List<LedgerEntryDraft> reversalEntries = correctionFactory.reverse(originalEntries);
		var correctionOutcome = service.post(new PostLedgerTransactionCommand(
			new RequestIdentity("integration-test", "correction-request"),
			new AuditTrace(
				new RequestIdentity("integration-test", "correction-request"),
				"corr-correction",
				null,
				new AuditTrace.Actor("integration", ActorType.SYSTEM),
				Instant.parse("2026-05-12T00:00:00Z")),
			"correction-1",
			"reverse original posting",
			null,
			reversalEntries));

		List<LedgerEntry> persistedOriginalEntries = persistenceAdapter.findEntriesByTransactionId(new LedgerTransactionId(UUID.fromString(originalTransactionId)));
		List<LedgerEntry> persistedCorrectionEntries = persistenceAdapter.findEntriesByTransactionId(new LedgerTransactionId(UUID.fromString(correctionOutcome.transactionId())));

		assertNotEquals(originalTransactionId, correctionOutcome.transactionId());
		assertEquals(2, persistedOriginalEntries.size());
		assertEquals(2, persistedCorrectionEntries.size());
		assertEquals(Direction.DEBIT, persistedOriginalEntries.getFirst().direction());
		assertEquals(Direction.CREDIT, persistedCorrectionEntries.getFirst().direction());
	}

	private com.bangnk.ledgercore.ledger_core.ledger.application.PostingOutcome postBalanced(String requestId) {
		return service.post(new PostLedgerTransactionCommand(
			new RequestIdentity("integration-test", requestId),
			new AuditTrace(
				new RequestIdentity("integration-test", requestId),
				"corr-" + requestId,
				null,
				new AuditTrace.Actor("integration", ActorType.SYSTEM),
				Instant.parse("2026-05-12T00:00:00Z")),
			"invoice-" + requestId,
			"balanced",
			null,
			List.of(
				draft("line-1", "cash", Direction.DEBIT, "100.0000"),
				draft("line-2", "revenue", Direction.CREDIT, "100.0000"))));
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
