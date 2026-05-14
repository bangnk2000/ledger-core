package com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class PostLedgerTransactionContractTest extends PostgresIntegrationTestBase {

	@Autowired
	private WebApplicationContext context;

	@Test
	void acceptsBalancedPosting() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();

		mvc.perform(post("/api/v1/ledger/postings")
				.header("Idempotency-Key", "request-001")
				.header("X-Requester-Scope", "contract-test")
				.header("X-Correlation-Id", "corr-1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "businessReference": "invoice-1001",
					  "description": "Record settlement",
					  "actor": {"actorId": "ledger-test", "actorType": "SYSTEM"},
					  "entries": [
					    {"lineId": "debit-cash", "accountId": "cash", "direction": "DEBIT", "amount": "100.0000", "currency": "USD"},
					    {"lineId": "credit-revenue", "accountId": "revenue", "direction": "CREDIT", "amount": "100.0000", "currency": "USD"}
					  ]
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.outcome").value("ACCEPTED"))
			.andExpect(jsonPath("$.code").value("LEDGER_POSTED"));
	}

	@Test
	void rejectsUnbalancedPosting() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();

		mvc.perform(post("/api/v1/ledger/postings")
				.header("Idempotency-Key", "request-002")
				.header("X-Requester-Scope", "contract-test")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "actor": {"actorId": "ledger-test", "actorType": "SYSTEM"},
					  "entries": [
					    {"lineId": "debit-cash", "accountId": "cash", "direction": "DEBIT", "amount": "100.0000", "currency": "USD"},
					    {"lineId": "credit-revenue", "accountId": "revenue", "direction": "CREDIT", "amount": "50.0000", "currency": "USD"}
					  ]
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.outcome").value("REJECTED"))
			.andExpect(jsonPath("$.code").value("LEDGER_UNBALANCED"));
	}

	@Test
	void rejectsPostingWithoutCreditLine() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();

		mvc.perform(post("/api/v1/ledger/postings")
				.header("Idempotency-Key", "request-003")
				.header("X-Requester-Scope", "contract-test")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "actor": {"actorId": "ledger-test", "actorType": "SYSTEM"},
					  "entries": [
					    {"lineId": "debit-cash-1", "accountId": "cash", "direction": "DEBIT", "amount": "100.0000", "currency": "USD"},
					    {"lineId": "debit-cash-2", "accountId": "cash", "direction": "DEBIT", "amount": "100.0000", "currency": "USD"}
					  ]
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.outcome").value("REJECTED"))
			.andExpect(jsonPath("$.code").value("LEDGER_MISSING_DIRECTION"));
	}
}
