package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class ReserveFundsContractTest extends BalanceApiContractSupport {

	@Autowired
	private WebApplicationContext context;

	@Test
	void reservesFundsWithExpectedContract() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
		mvc.perform(post("/api/v1/balances/reservations")
				.header("Idempotency-Key", "reserve-contract-1")
				.header("X-Requester-Scope", "balance-contract")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "accountId": "balance-contract-account",
					  "currency": "USD",
					  "amount": "1.0000",
					  "direction": "CREDIT",
					  "businessReference": "ord-1",
					  "expiresAt": "2026-05-15T10:00:00Z",
					  "actor": { "actorId": "contract-user", "actorType": "SYSTEM" }
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.outcome").value("ACCEPTED"))
			.andExpect(jsonPath("$.code").value("BALANCE_RESERVED"));
	}
}
