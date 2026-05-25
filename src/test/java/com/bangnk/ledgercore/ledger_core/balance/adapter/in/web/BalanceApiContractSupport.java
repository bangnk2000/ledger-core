package com.bangnk.ledgercore.ledger_core.balance.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.balance.adapter.PostgresIntegrationTestSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public abstract class BalanceApiContractSupport extends PostgresIntegrationTestSupport {

	protected static final String IDEMPOTENCY_KEY = "Idempotency-Key";
	protected static final String REQUESTER_SCOPE = "X-Requester-Scope";
	protected static final String CORRELATION_ID = "X-Correlation-Id";

	@Autowired
	private ObjectMapper objectMapper;

	protected String json(Object body) {
		try {
			return objectMapper.writeValueAsString(body);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Unable to serialize contract request body", ex);
		}
	}

	protected MockHttpServletRequestBuilder balanceJsonRequest(
			MockHttpServletRequestBuilder request,
			String requestId,
			String requesterScope) {
		return request
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.header(IDEMPOTENCY_KEY, requestId)
			.header(REQUESTER_SCOPE, requesterScope);
	}

	protected MockHttpServletRequestBuilder correlatedBalanceJsonRequest(
			MockHttpServletRequestBuilder request,
			String requestId,
			String requesterScope,
			String correlationId) {
		return balanceJsonRequest(request, requestId, requesterScope)
			.header(CORRELATION_ID, correlationId);
	}
}
