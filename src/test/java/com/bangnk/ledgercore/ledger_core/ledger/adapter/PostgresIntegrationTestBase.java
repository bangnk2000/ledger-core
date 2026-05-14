package com.bangnk.ledgercore.ledger_core.ledger.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
public abstract class PostgresIntegrationTestBase {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>("postgres:16-alpine")
		.withDatabaseName("ledger_core")
		.withUsername("ledger")
		.withPassword("ledger");

	static {
		POSTGRESQL.start();
	}

	@DynamicPropertySource
	static void postgresProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRESQL::getUsername);
		registry.add("spring.datasource.password", POSTGRESQL::getPassword);
	}

	@BeforeEach
	void resetLedgerTables() {
		jdbcTemplate.execute("TRUNCATE TABLE ledger_entries, ledger_transactions, ledger_idempotency_records");
	}
}
