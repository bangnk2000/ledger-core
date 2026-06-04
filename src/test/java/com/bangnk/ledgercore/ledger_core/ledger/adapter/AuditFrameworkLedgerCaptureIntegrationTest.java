package com.bangnk.ledgercore.ledger_core.ledger.adapter;

import static org.assertj.core.api.Assertions.assertThat;

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
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class AuditFrameworkLedgerCaptureIntegrationTest extends PostgresIntegrationTestBase {

	@Autowired
	private PostLedgerTransactionService service;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void storesSharedAuditEvidenceForAcceptedPosting() {
		var requestId = "req-ledger-audit-1";
		var outcome = service.post(new PostLedgerTransactionCommand(
			new RequestIdentity("integration", requestId),
			new AuditTrace(
				new RequestIdentity("integration", requestId),
				"corr-" + requestId,
				"cause-" + requestId,
				new AuditTrace.Actor("integration-user", ActorType.SYSTEM),
				Instant.parse("2026-06-04T12:00:00Z")),
			"invoice-" + requestId,
			"shared audit capture",
			Map.of("channel", "api"),
			List.of(
				draft("line-1", "cash", Direction.DEBIT, "100.0000"),
				draft("line-2", "revenue", Direction.CREDIT, "100.0000"))));

		assertThat(outcome.code()).isEqualTo("LEDGER_POSTED");
		assertThat(jdbcTemplate.queryForObject(
			"select module_name from audit_events where subject_id = ?",
			String.class,
			outcome.transactionId())).isEqualTo("ledger");
		assertThat(jdbcTemplate.queryForObject(
			"select idempotency_key from audit_events where subject_id = ?",
			String.class,
			outcome.transactionId())).isEqualTo(requestId);
		assertThat(jdbcTemplate.queryForObject(
			"select count(*) from audit_publication_attempts",
			Integer.class)).isEqualTo(1);
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
