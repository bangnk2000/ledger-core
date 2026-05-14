package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter;

import java.util.List;
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
public abstract class PostgresIntegrationTestSupport {

	private static final List<String> BALANCE_TABLES = List.of(
		"balance_idempotency_records",
		"balance_reconciliation_records",
		"balance_rebuild_checkpoints",
		"balance_rebuild_jobs",
		"balance_snapshots",
		"funds_reservations",
		"balance_state");

	static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>("postgres:16-alpine")
		.withDatabaseName("ledger_core")
		.withUsername("ledger")
		.withPassword("ledger");

	static {
		POSTGRESQL.start();
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@DynamicPropertySource
	static void postgresProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRESQL::getUsername);
		registry.add("spring.datasource.password", POSTGRESQL::getPassword);
	}

	@BeforeEach
	void resetBalanceTables() {
		var existingTables = jdbcTemplate.queryForList("""
			SELECT table_name
			FROM information_schema.tables
			WHERE table_schema = 'public'
			""", String.class).stream()
			.filter(BALANCE_TABLES::contains)
			.toList();

		if (!existingTables.isEmpty()) {
			jdbcTemplate.execute("TRUNCATE TABLE " + String.join(", ", existingTables) + " RESTART IDENTITY CASCADE");
		}
	}
}
