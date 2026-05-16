package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class BalanceRebuildContractTest extends BalanceApiContractSupport {

	@Autowired
	private WebApplicationContext context;

	@Test
	void startsAndGetsRebuildJobContract() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
		MvcResult started = mvc.perform(post("/api/v1/balances/rebuild-jobs")
				.contentType("application/json")
				.content("""
					{
					  "scope": "acc-rebuild-contract",
					  "replayContractVersion": "v1",
					  "restartFromCheckpoint": true
					}
					"""))
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.jobId").exists())
			.andExpect(jsonPath("$.status").value("SUCCEEDED"))
			.andReturn();
		String jobId = started.getResponse().getContentAsString().replaceAll(".*\"jobId\":\"([^\"]+)\".*", "$1");

		mvc.perform(get("/api/v1/balances/rebuild-jobs/{jobId}", jobId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.jobId").value(jobId))
			.andExpect(jsonPath("$.status").exists())
			.andExpect(jsonPath("$.scope").value("acc-rebuild-contract"));
	}
}
