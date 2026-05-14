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

class PostLedgerTransactionTraceContractTest extends PostgresIntegrationTestBase {

	@Autowired
	private WebApplicationContext context;

	@Test
	void returnsSanitizedRejectedOutcomeWithSafeTraceDetails() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();

		mvc.perform(post("/api/v1/ledger/postings")
				.header("Idempotency-Key", "trace-rejected-001")
				.header("X-Requester-Scope", "contract-test")
				.header("X-Correlation-Id", "corr-trace-001")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "businessReference": "invoice-trace-1",
					  "description": "invalid trace example",
					  "causationId": "cause-trace-001",
					  "metadata": {"internalFlag": "ignored"},
					  "actor": {"actorId": "ledger-test", "actorType": "SYSTEM"},
					  "entries": [
					    {"lineId": "debit-cash", "accountId": "cash", "direction": "DEBIT", "amount": "100.0000", "currency": "USD"},
					    {"lineId": "credit-revenue", "accountId": "revenue", "direction": "CREDIT", "amount": "90.0000", "currency": "USD"}
					  ]
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.outcome").value("REJECTED"))
			.andExpect(jsonPath("$.code").value("LEDGER_UNBALANCED"))
			.andExpect(jsonPath("$.message").value("Debit and credit totals must balance"))
			.andExpect(jsonPath("$.trace.correlationId").value("corr-trace-001"))
			.andExpect(jsonPath("$.trace.causationId").value("cause-trace-001"))
			.andExpect(jsonPath("$.trace.actorId").value("ledger-test"))
			.andExpect(jsonPath("$.trace.actorType").value("SYSTEM"));
	}
}
