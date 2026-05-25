package com.bangnk.ledgercore.ledger_core.balance.application;

import com.bangnk.ledgercore.ledger_core.balance.application.BalanceApplicationErrors.BalanceDomainException;
import com.bangnk.ledgercore.ledger_core.balance.application.BalanceApplicationErrors.BalanceOutcomeType;
import java.util.function.Supplier;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
public class ProtectedWriteRetryExecutor {

	private final BalanceRetryPolicy retryPolicy;
	private final BalanceObservability observability;

	public ProtectedWriteRetryExecutor(BalanceObservability observability) {
		this.retryPolicy = BalanceRetryPolicy.defaults();
		this.observability = observability;
	}

	public <T> T execute(Supplier<T> action) {
		int attempt = 1;
		while (true) {
			try {
				return action.get();
			} catch (RuntimeException ex) {
				if (!isRetriable(ex)) {
					throw ex;
				}
				observability.recordContention();
				if (!retryPolicy.shouldRetry(attempt)) {
					observability.recordFailClosed();
					throw new BalanceDomainException(
						BalanceOutcomeType.LOCKED,
						"BALANCE_LOCK_TIMEOUT",
						"Protected write exceeded retry budget under contention");
				}
				observability.recordRetry(attempt);
				sleep(retryPolicy.backoffForAttempt(attempt).toMillis());
				attempt++;
			}
		}
	}

	private boolean isRetriable(RuntimeException ex) {
		return ex instanceof CannotAcquireLockException
			|| ex instanceof PessimisticLockingFailureException
			|| ex instanceof DeadlockLoserDataAccessException;
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
			throw new BalanceDomainException(
				BalanceOutcomeType.FAILED,
				"BALANCE_RETRY_INTERRUPTED",
				"Retry execution interrupted");
		}
	}
}
