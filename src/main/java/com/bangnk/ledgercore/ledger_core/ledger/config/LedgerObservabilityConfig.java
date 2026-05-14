package com.bangnk.ledgercore.ledger_core.ledger.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LedgerObservabilityConfig {

	@Bean
	Clock ledgerClock() {
		return Clock.systemUTC();
	}

	@Bean
	LedgerMetrics ledgerMetrics(MeterRegistry meterRegistry) {
		return new LedgerMetrics(meterRegistry);
	}

	public static final class LedgerMetrics {
		private final MeterRegistry meterRegistry;
		private final Timer postingTimer;
		private final Timer balanceTimer;

		public LedgerMetrics(MeterRegistry meterRegistry) {
			this.meterRegistry = meterRegistry;
			this.postingTimer = Timer.builder("ledger.postings.duration").register(meterRegistry);
			this.balanceTimer = Timer.builder("ledger.balance.duration").register(meterRegistry);
		}

		public Counter postingOutcome(String outcome) {
			return meterRegistry.counter("ledger.postings", "outcome", outcome);
		}

		public Counter balanceOutcome(String outcome) {
			return meterRegistry.counter("ledger.balance.requests", "outcome", outcome);
		}

		public Timer postingTimer() {
			return postingTimer;
		}

		public Timer balanceTimer() {
			return balanceTimer;
		}
	}
}
