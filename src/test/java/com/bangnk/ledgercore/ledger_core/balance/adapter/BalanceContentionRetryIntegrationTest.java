package com.bangnk.ledgercore.ledger_core.balance.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bangnk.ledgercore.ledger_core.balance.application.BalanceApplicationErrors.BalanceDomainException;
import com.bangnk.ledgercore.ledger_core.balance.application.ProtectedWriteRetryExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;

class BalanceContentionRetryIntegrationTest extends PostgresIntegrationTestSupport {

	@Autowired
	private ProtectedWriteRetryExecutor retryExecutor;

	@Test
	void retriesOnTransientLockContentionAndSucceedsWithinPolicy() {
		AtomicInteger attempts = new AtomicInteger(0);

		String result = retryExecutor.execute(() -> {
			if (attempts.incrementAndGet() < 3) {
				throw new CannotAcquireLockException("simulated lock timeout");
			}
			return "ok";
		});

		assertThat(result).isEqualTo("ok");
		assertThat(attempts.get()).isEqualTo(3);
	}

	@Test
	void failsWithStableLockedOutcomeAfterRetryBudgetExhausted() {
		assertThatThrownBy(() -> retryExecutor.execute(() -> {
			throw new CannotAcquireLockException("simulated lock timeout");
		}))
			.isInstanceOf(BalanceDomainException.class)
			.satisfies(ex -> {
				BalanceDomainException failure = (BalanceDomainException) ex;
				assertThat(failure.getCode()).isEqualTo("BALANCE_LOCK_TIMEOUT");
				assertThat(failure.getOutcome().name()).isEqualTo("LOCKED");
			});
	}
}
