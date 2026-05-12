package com.bangnk.ledgercore.ledger_core.ledger.application.command;

import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity.RequestHash;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

@Component
public class PostingRequestHasher {

	private final ObjectMapper objectMapper;

	public PostingRequestHasher(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper.copy()
			.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
			.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
	}

	public RequestHash hash(PostLedgerTransactionCommand command) {
		try {
			byte[] payload = objectMapper.writeValueAsString(command).getBytes(StandardCharsets.UTF_8);
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(payload);
			StringBuilder hex = new StringBuilder();
			for (byte b : hash) {
				hex.append(String.format("%02x", b));
			}
			return new RequestHash(hex.toString());
		} catch (JsonProcessingException | NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Unable to hash posting request", ex);
		}
	}
}
