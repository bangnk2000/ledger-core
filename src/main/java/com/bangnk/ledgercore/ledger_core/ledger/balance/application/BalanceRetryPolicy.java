package com.bangnk.ledgercore.ledger_core.ledger.balance.application;

import java.time.Duration;

public record BalanceRetryPolicy(
		int maxAttempts,
		Duration initialBackoff,
		Duration maxBackoff,
		double backoffMultiplier
) {

	public static BalanceRetryPolicy defaults() {
		return new BalanceRetryPolicy(3, Duration.ofMillis(25), Duration.ofMillis(200), 2.0d);
	}

	public boolean shouldRetry(int attempt) {
		return attempt < maxAttempts;
	}

	public Duration backoffForAttempt(int attempt) {
		double scaled = initialBackoff.toMillis() * Math.pow(backoffMultiplier, Math.max(0, attempt - 1));
		long millis = Math.min((long) scaled, maxBackoff.toMillis());
		return Duration.ofMillis(Math.max(0, millis));
	}
}
