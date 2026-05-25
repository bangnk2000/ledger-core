package com.bangnk.ledgercore.ledger_core.balance.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class GetCurrentBalanceContractTest extends BalanceApiContractSupport {

	@Autowired
	private WebApplicationContext context;

	@Test
	void returnsCurrentBalanceContract() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
		mvc.perform(get("/api/v1/balances/accounts/{accountId}/current", "balance-contract-account")
				.queryParam("currency", "USD")
				.queryParam("consistency", "STRONG"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accountId").value("balance-contract-account"))
			.andExpect(jsonPath("$.currency").value("USD"))
			.andExpect(jsonPath("$.availableBalance").exists())
			.andExpect(jsonPath("$.snapshotVersion").exists())
			.andExpect(jsonPath("$.asOfSequence").exists())
			.andExpect(jsonPath("$.consistencyMode").value("STRONG"));
	}
}
