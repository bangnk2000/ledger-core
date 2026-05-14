package com.bangnk.ledgercore.ledger_core.ledger.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.JpaLedgerPersistenceAdapter;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionCommand;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionService;
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
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class LedgerTraceRetentionIntegrationTest extends PostgresIntegrationTestBase {

	@Autowired
	private PostLedgerTransactionService postingService;

	@Autowired
	private JpaLedgerPersistenceAdapter persistenceAdapter;

	@Test
	void retainsTransactionAndEntryTraceDetailsForAcceptedPosting() {
		RequestIdentity identity = new RequestIdentity("trace-integration", "trace-request-1");
		PostLedgerTransactionCommand command = new PostLedgerTransactionCommand(
			identity,
			new AuditTrace(
				identity,
				"corr-trace-1",
				"cause-trace-1",
				new AuditTrace.Actor("trace-user", ActorType.USER),
				Instant.parse("2026-05-12T00:00:00Z")),
			"invoice-42",
			"trace retention",
			Map.of("source", "billing", "batchId", "batch-7"),
			List.of(
				draft("line-debit", "cash", Direction.DEBIT, "125.0000", Map.of("lineType", "principal")),
				draft("line-credit", "revenue", Direction.CREDIT, "125.0000", Map.of("lineType", "offset"))));

		var outcome = postingService.post(command);
		var transaction = persistenceAdapter.findById(new LedgerTransactionId(UUID.fromString(outcome.transactionId()))).orElseThrow();
		var entries = persistenceAdapter.findEntriesByTransactionId(new LedgerTransactionId(UUID.fromString(outcome.transactionId())));

		assertThat(transaction.businessReference()).isEqualTo("invoice-42");
		assertThat(transaction.description()).isEqualTo("trace retention");
		assertThat(transaction.metadata()).containsEntry("source", "billing").containsEntry("batchId", "batch-7");
		assertThat(transaction.auditTrace().requestIdentity()).isEqualTo(identity);
		assertThat(transaction.auditTrace().correlationId()).isEqualTo("corr-trace-1");
		assertThat(transaction.auditTrace().causationId()).isEqualTo("cause-trace-1");
		assertThat(transaction.auditTrace().actor().actorId()).isEqualTo("trace-user");
		assertThat(transaction.auditTrace().actor().actorType()).isEqualTo(ActorType.USER);
		assertThat(entries).hasSize(2);
		assertThat(entries)
			.extracting(entry -> entry.metadata().get("lineType"))
			.containsExactlyInAnyOrder("principal", "offset");
	}

	private static LedgerEntryDraft draft(String lineId, String accountId, Direction direction, String amount, Map<String, Object> metadata) {
		return new LedgerEntryDraft(
			new LineId(lineId),
			new AccountId(accountId),
			direction,
			new Money(new BigDecimal(amount), "USD"),
			metadata);
	}
}
