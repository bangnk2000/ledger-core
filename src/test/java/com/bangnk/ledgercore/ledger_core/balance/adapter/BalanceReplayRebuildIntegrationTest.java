package com.bangnk.ledgercore.ledger_core.balance.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.balance.application.BalanceRebuildService;
import com.bangnk.ledgercore.ledger_core.balance.application.port.in.BalanceRebuildUseCase.StartRebuildCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class BalanceReplayRebuildIntegrationTest extends PostgresIntegrationTestSupport {

	@Autowired
	private BalanceRebuildService rebuildService;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void startsAndPersistsRebuildJobWithCheckpoint() {
		var job = rebuildService.start(new StartRebuildCommand("acc-rebuild-int", "v1", true, "integration"));
		assertThat(job.status().name()).isEqualTo("SUCCEEDED");
		assertThat(jdbcTemplate.queryForObject("select count(*) from balance_rebuild_jobs where job_id = ?", Integer.class, job.jobId()))
			.isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject("select count(*) from balance_rebuild_checkpoints where job_id = ?", Integer.class, job.jobId()))
			.isEqualTo(1);
	}
}
