package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.BalanceReconciliationService;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in.BalanceReconciliationUseCase.StartReconciliationCommand;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class BalanceReconciliationIntegrationTest extends PostgresIntegrationTestSupport {

	@Autowired
	private BalanceReconciliationService reconciliationService;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void recordsDriftWhenAvailableBalanceIsNegative() {
		jdbcTemplate.update("""
			INSERT INTO balance_state (
				account_id, currency, ledger_balance, locked_amount, pending_debit_amount, pending_credit_amount,
				version, ledger_as_of_sequence, reservation_as_of_sequence, reconciliation_status, updated_at
			) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
			""", "acc-recon-int", "USD", new BigDecimal("1.0000"), new BigDecimal("2.0000"), BigDecimal.ZERO, BigDecimal.ZERO, 1L, 1L, 1L,
			"HEALTHY", Timestamp.from(Instant.parse("2026-05-16T11:00:00Z")));

		var accepted = reconciliationService.start(new StartReconciliationCommand("acc-recon-int", false));
		assertThat(accepted.status()).isEqualTo("ACCEPTED");
		assertThat(jdbcTemplate.queryForObject("select count(*) from balance_reconciliation_records where account_scope = ?", Integer.class,
			"acc-recon-int")).isEqualTo(1);
	}
}
