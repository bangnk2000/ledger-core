package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.support.ReplayStateAssertions;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.BalanceRebuildService;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in.BalanceRebuildUseCase.StartRebuildCommand;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.LedgerReplayExportPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.LedgerReplayExportPort.ReplayBatch;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.LedgerReplayExportPort.ReplayCursor;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.LedgerReplayExportPort.ReplayRecord;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.MoneyAmount;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = BalanceReplayDeterminismIntegrationTest.ReplayFixtureTestConfig.class)
class BalanceReplayDeterminismIntegrationTest extends PostgresIntegrationTestSupport {

	@Autowired
	private BalanceRebuildService rebuildService;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void replayingSameFixtureTwicePersistsEquivalentState() {
		var firstRun = rebuildService.start(new StartRebuildCommand("fixture-replay", "golden-v1", true, "integration"));
		assertThat(firstRun.status().name()).isEqualTo("SUCCEEDED");
		Map<String, List<Map<String, Object>>> firstDomainState = snapshotDomainState();
		resetDomainStateTables();

		var secondRun = rebuildService.start(new StartRebuildCommand("fixture-replay", "golden-v1", true, "integration"));
		assertThat(secondRun.status().name()).isEqualTo("SUCCEEDED");
		Map<String, List<Map<String, Object>>> secondDomainState = snapshotDomainState();

		ReplayStateAssertions.assertBaselineMatchesRefactor(firstDomainState, secondDomainState);
	}

	private Map<String, List<Map<String, Object>>> snapshotDomainState() {
		Map<String, List<Map<String, Object>>> snapshot = new LinkedHashMap<>();
		snapshot.put("balance_state", jdbcTemplate.queryForList("""
			select account_id, currency, ledger_balance, locked_amount, pending_debit_amount, pending_credit_amount,
			       available_balance, version, ledger_as_of_sequence, reservation_as_of_sequence, reconciliation_status, updated_at
			from balance_state
			order by account_id, currency
			"""));
		snapshot.put("funds_reservations", jdbcTemplate.queryForList("""
			select reservation_id, requester_scope, request_id, account_id, currency, direction, amount, business_reference,
			       status, expires_at, ledger_transaction_id, confirmation_reference, created_at, updated_at
			from funds_reservations
			order by reservation_id
			"""));
		snapshot.put("balance_snapshots", jdbcTemplate.queryForList("""
			select snapshot_id, account_id, currency, ledger_balance, locked_amount, pending_debit_amount, pending_credit_amount,
			       available_balance, snapshot_version, as_of_sequence, as_of_time, consistency_mode, reconciliation_status, created_at
			from balance_snapshots
			order by snapshot_id
			"""));
		return snapshot;
	}

	private void resetDomainStateTables() {
		jdbcTemplate.execute("TRUNCATE TABLE funds_reservations, balance_snapshots, balance_state RESTART IDENTITY CASCADE");
	}

	@TestConfiguration
	static class ReplayFixtureTestConfig {

		@Bean
		@Primary
		LedgerReplayExportPort fixtureReplayExportPort() throws IOException {
			List<ReplayRecord> records = loadFixture("ledger/replay/golden-replay-v1.csv");
			return (cursor, limit) -> new ReplayBatch(records, new ReplayCursor(42L, "fixture-golden-v1"), true);
		}

		private List<ReplayRecord> loadFixture(String path) throws IOException {
			String content = new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
			return content.lines()
				.skip(1)
				.filter(line -> !line.isBlank())
				.map(this::parseRecord)
				.toList();
		}

		private ReplayRecord parseRecord(String line) {
			String[] cells = line.split(",", -1);
			return new ReplayRecord(
				Long.parseLong(cells[0]),
				cells[1],
				cells[2],
				new AccountId(cells[3]),
				new CurrencyCode(cells[4]),
				BalanceDirection.valueOf(cells[5]),
				new MoneyAmount(new BigDecimal(cells[6])),
				Instant.parse(cells[7]));
		}
	}
}
