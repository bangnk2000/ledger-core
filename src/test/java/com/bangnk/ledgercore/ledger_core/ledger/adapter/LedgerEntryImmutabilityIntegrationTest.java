package com.bangnk.ledgercore.ledger_core.ledger.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.JpaLedgerPersistenceAdapter;
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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

class LedgerEntryImmutabilityIntegrationTest extends PostgresIntegrationTestBase {

	@Autowired
	private PostLedgerTransactionService service;

	@Autowired
	private JpaLedgerPersistenceAdapter persistenceAdapter;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void blocksUpdateAndDeleteOfPostedEntries() {
		String transactionId = postBalanced("immutability-request");
		LedgerEntry originalEntry = persistenceAdapter.findEntriesByTransactionId(new LedgerTransactionId(UUID.fromString(transactionId))).getFirst();

		assertThrows(DataAccessException.class, () -> jdbcTemplate.update(
			"update ledger_entries set amount = ? where id = ?",
			new BigDecimal("200.0000"),
			originalEntry.id().value()));
		assertThrows(DataAccessException.class, () -> jdbcTemplate.update(
			"delete from ledger_entries where id = ?",
			originalEntry.id().value()));

		LedgerEntry storedEntry = persistenceAdapter.findEntriesByTransactionId(new LedgerTransactionId(UUID.fromString(transactionId))).getFirst();
		assertEquals(new BigDecimal("100.0000"), storedEntry.money().amount());
		assertEquals(Direction.DEBIT, storedEntry.direction());
		assertEquals("cash", storedEntry.accountId().value());
	}

	private String postBalanced(String requestId) {
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
				draft("line-2", "revenue", Direction.CREDIT, "100.0000")))).transactionId();
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
