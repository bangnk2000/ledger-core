package com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.PostgresIntegrationTestBase;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class GetAccountBalanceContractTest extends PostgresIntegrationTestBase {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private PostLedgerTransactionService postingService;

	@Test
	void returnsBalanceForExistingAccount() throws Exception {
		postingService.post(command("balance-contract-1", "cash", Direction.DEBIT, "100.0000", "revenue"));
		postingService.post(command("balance-contract-2", "cash", Direction.CREDIT, "30.0000", "expense"));

		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
		mvc.perform(get("/api/v1/ledger/accounts/cash/balance").queryParam("currency", "USD"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accountId").value("cash"))
			.andExpect(jsonPath("$.balance").value("70.0000"))
			.andExpect(jsonPath("$.currency").value("USD"))
			.andExpect(jsonPath("$.entryCount").value(2))
			.andExpect(jsonPath("$.calculatedAt").isNotEmpty());
	}

	@Test
	void returnsZeroForAccountWithoutPostedEntries() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();

		mvc.perform(get("/api/v1/ledger/accounts/no-entry-account/balance").queryParam("currency", "USD"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accountId").value("no-entry-account"))
			.andExpect(jsonPath("$.balance").value("0.0000"))
			.andExpect(jsonPath("$.currency").value("USD"))
			.andExpect(jsonPath("$.entryCount").value(0))
			.andExpect(jsonPath("$.calculatedAt").isNotEmpty());
	}

	private static PostLedgerTransactionCommand command(String requestId, String accountId, Direction direction, String amount, String offsetAccountId) {
		RequestIdentity identity = new RequestIdentity("balance-contract", requestId);
		Direction offsetDirection = direction == Direction.DEBIT ? Direction.CREDIT : Direction.DEBIT;
		return new PostLedgerTransactionCommand(
			identity,
			new AuditTrace(
				identity,
				"corr-" + requestId,
				null,
				new AuditTrace.Actor("contract-test", ActorType.SYSTEM),
				Instant.parse("2026-05-12T00:00:00Z")),
			"balance-" + requestId,
			"balance seed " + requestId,
			null,
			List.of(
				draft("target-" + requestId, accountId, direction, amount),
				draft("offset-" + requestId, offsetAccountId, offsetDirection, amount)));
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
