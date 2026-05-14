package com.bangnk.ledgercore.ledger_core.ledger.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class LedgerModuleConfig {

	@Bean
	ObjectMapper objectMapper() {
		return new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}
}
