package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class BalanceReconciliationContractTest extends BalanceApiContractSupport {

	@Autowired
	private WebApplicationContext context;

	@Test
	void startsReconciliationContract() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
		mvc.perform(post("/api/v1/balances/reconciliations")
				.contentType("application/json")
				.content("""
					{
					  "scope": "acc-recon-contract",
					  "rebuildIfDriftDetected": false
					}
					"""))
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.reconciliationRunId").exists())
			.andExpect(jsonPath("$.status").value("ACCEPTED"));
	}
}
