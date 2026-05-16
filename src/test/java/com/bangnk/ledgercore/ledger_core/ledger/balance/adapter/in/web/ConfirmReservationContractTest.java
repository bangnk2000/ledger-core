package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class ConfirmReservationContractTest extends BalanceApiContractSupport {

	@Autowired
	private WebApplicationContext context;

	@Test
	void confirmsReservationWithExpectedContract() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
		MvcResult result = mvc.perform(balanceJsonRequest(post("/api/v1/balances/reservations"), "reserve-contract-confirm-1", "balance-contract")
				.content("""
					{
					  "accountId": "balance-contract-account-c1",
					  "currency": "USD",
					  "amount": "1.0000",
					  "direction": "CREDIT",
					  "businessReference": "ord-c1",
					  "expiresAt": "2026-05-30T10:00:00Z",
					  "actor": { "actorId": "contract-user", "actorType": "SYSTEM" }
					}
					"""))
			.andExpect(status().isCreated())
			.andReturn();
		String reservationId = result.getResponse().getContentAsString().replaceAll(".*\"reservationId\":\"([^\"]+)\".*", "$1");

		mvc.perform(balanceJsonRequest(post("/api/v1/balances/reservations/{reservationId}/confirm", reservationId), "confirm-contract-1", "balance-contract")
				.content(json(new ReservationDtos.ConfirmReservationRequest(
					ReservationDtos.ConfirmReservationRequest.FinalizationType.POST_CREDIT,
					"ledg-txn-1",
					new ReservationDtos.ActorDto("contract-user", com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceActorType.SYSTEM, null)))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.outcome").value("ACCEPTED"))
			.andExpect(jsonPath("$.code").value("BALANCE_RESERVATION_CONFIRMED"));
	}
}
