package com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.PostgresIntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class PostLedgerTransactionIdempotencyContractTest extends PostgresIntegrationTestBase {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void returnsStableDuplicateOutcomeForSameRequestContent() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
		String payload = """
			{
			  "businessReference": "invoice-duplicate-1",
			  "description": "duplicate-safe posting",
			  "actor": {"actorId": "ledger-test", "actorType": "SYSTEM"},
			  "entries": [
			    {"lineId": "debit-cash", "accountId": "cash", "direction": "DEBIT", "amount": "100.0000", "currency": "USD"},
			    {"lineId": "credit-revenue", "accountId": "revenue", "direction": "CREDIT", "amount": "100.0000", "currency": "USD"}
			  ]
			}
			""";

		JsonNode first = objectMapper.readTree(mvc.perform(post("/api/v1/ledger/postings")
				.header("Idempotency-Key", "request-duplicate-001")
				.header("X-Requester-Scope", "contract-test")
				.header("X-Correlation-Id", "corr-duplicate-1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.outcome").value("ACCEPTED"))
			.andExpect(jsonPath("$.code").value("LEDGER_POSTED"))
			.andReturn()
			.getResponse()
			.getContentAsString());

		JsonNode duplicate = objectMapper.readTree(mvc.perform(post("/api/v1/ledger/postings")
				.header("Idempotency-Key", "request-duplicate-001")
				.header("X-Requester-Scope", "contract-test")
				.header("X-Correlation-Id", "corr-duplicate-1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.outcome").value("DUPLICATE"))
			.andExpect(jsonPath("$.code").value("LEDGER_POSTED"))
			.andReturn()
			.getResponse()
			.getContentAsString());

		assertThat(duplicate.get("transactionId").asText()).isEqualTo(first.get("transactionId").asText());
		assertThat(duplicate.get("postedAt").asText()).isNotBlank();
	}

	@Test
	void returnsConflictForSameRequestIdentityWithDifferentContent() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();

		mvc.perform(post("/api/v1/ledger/postings")
				.header("Idempotency-Key", "request-conflict-001")
				.header("X-Requester-Scope", "contract-test")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "businessReference": "invoice-conflict-1",
					  "actor": {"actorId": "ledger-test", "actorType": "SYSTEM"},
					  "entries": [
					    {"lineId": "debit-cash", "accountId": "cash", "direction": "DEBIT", "amount": "100.0000", "currency": "USD"},
					    {"lineId": "credit-revenue", "accountId": "revenue", "direction": "CREDIT", "amount": "100.0000", "currency": "USD"}
					  ]
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.outcome").value("ACCEPTED"));

		mvc.perform(post("/api/v1/ledger/postings")
				.header("Idempotency-Key", "request-conflict-001")
				.header("X-Requester-Scope", "contract-test")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "businessReference": "invoice-conflict-2",
					  "actor": {"actorId": "ledger-test", "actorType": "SYSTEM"},
					  "entries": [
					    {"lineId": "debit-cash", "accountId": "cash", "direction": "DEBIT", "amount": "100.0000", "currency": "USD"},
					    {"lineId": "credit-revenue", "accountId": "revenue", "direction": "CREDIT", "amount": "90.0000", "currency": "USD"},
					    {"lineId": "credit-adjustment", "accountId": "adjustment", "direction": "CREDIT", "amount": "10.0000", "currency": "USD"}
					  ]
					}
					"""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.outcome").value("CONFLICT"))
			.andExpect(jsonPath("$.code").value("LEDGER_IDEMPOTENCY_CONFLICT"));
	}
}
