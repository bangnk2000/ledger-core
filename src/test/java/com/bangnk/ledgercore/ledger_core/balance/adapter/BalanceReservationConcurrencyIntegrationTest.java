package com.bangnk.ledgercore.ledger_core.balance.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.bangnk.ledgercore.ledger_core.balance.application.ReserveFundsService;
import com.bangnk.ledgercore.ledger_core.balance.application.command.BalanceMutationRequest;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceActorType;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class BalanceReservationConcurrencyIntegrationTest extends PostgresIntegrationTestSupport {

	@Autowired
	private ReserveFundsService reserveFundsService;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void handlesConcurrentOverlappingDebitReservations() throws Exception {
		assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
			ExecutorService executor = Executors.newFixedThreadPool(16);
			try {
				List<Callable<String>> tasks = new ArrayList<>();
				for (int i = 0; i < 100; i++) {
					int idx = i;
					tasks.add(() -> reserveFundsService.reserve(request("shared-account", "rq-" + idx, "1.0000")).outcome().outcome().name());
				}
				List<Future<String>> futures = executor.invokeAll(tasks, 5, TimeUnit.SECONDS);
				assertThat(futures).allMatch(Future::isDone);
				assertThat(futures).noneMatch(Future::isCancelled);

				long accepted = 0;
				long rejected = 0;
				for (Future<String> future : futures) {
					String outcome = future.get(1, TimeUnit.SECONDS);
					if ("ACCEPTED".equals(outcome)) {
						accepted++;
					}
					if ("REJECTED".equals(outcome)) {
						rejected++;
					}
				}
				assertThat(accepted).isZero();
				assertThat(rejected).isEqualTo(100);
				assertThat(jdbcTemplate.queryForObject("select count(*) from funds_reservations", Integer.class)).isEqualTo(0);
				assertThat(jdbcTemplate.queryForObject("select count(*) from balance_state", Integer.class)).isEqualTo(0);
			} finally {
				executor.shutdownNow();
				executor.awaitTermination(5, TimeUnit.SECONDS);
			}
		});
	}

	private static BalanceMutationRequest request(String accountId, String requestId, String amount) {
		return new BalanceMutationRequest(
			new RequestIdentity("concurrency", requestId),
			BalanceMutationType.RESERVE,
			List.of(new AccountId(accountId)),
			new CurrencyCode("USD"),
			new MoneyAmount(new BigDecimal(amount)),
			BalanceDirection.DEBIT,
			new ActorContext("it-user", BalanceActorType.SYSTEM, "corr-" + requestId, null),
			"order-" + requestId,
			Instant.parse("2026-05-20T12:00:00Z"));
	}
}
