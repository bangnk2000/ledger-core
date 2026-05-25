package com.bangnk.ledgercore.ledger_core.balance.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceActorType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class ReleaseReservationContractTest extends BalanceApiContractSupport {

	@Autowired
	private WebApplicationContext context;

	@Test
	void releasesReservationWithExpectedContract() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
		String reservationId = createCreditReservation(mvc);
		mvc.perform(balanceJsonRequest(post("/api/v1/balances/reservations/{reservationId}/release", reservationId), "release-contract-1", "balance-contract")
				.content(json(new ReservationDtos.ReleaseReservationRequest(
					ReservationDtos.ReleaseReservationRequest.ReleaseReason.CANCELLED,
					new ReservationDtos.ActorDto("contract-user", BalanceActorType.SYSTEM, null)))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.outcome").value("ACCEPTED"))
			.andExpect(jsonPath("$.code").value("BALANCE_RESERVATION_RELEASED"));
	}

	private String createCreditReservation(MockMvc mvc) throws Exception {
		MvcResult result = mvc.perform(balanceJsonRequest(post("/api/v1/balances/reservations"), "reserve-contract-release-1", "balance-contract")
				.content("""
					{
					  "accountId": "balance-contract-account-r1",
					  "currency": "USD",
					  "amount": "1.0000",
					  "direction": "CREDIT",
					  "businessReference": "ord-r1",
					  "expiresAt": "2026-05-30T10:00:00Z",
					  "actor": { "actorId": "contract-user", "actorType": "SYSTEM" }
					}
					"""))
			.andExpect(status().isCreated())
			.andReturn();
		return result.getResponse().getContentAsString().replaceAll(".*\"reservationId\":\"([^\"]+)\".*", "$1");
	}
}
