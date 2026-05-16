package com.bangnk.ledgercore.ledger_core.ledger.balance.application;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.BalanceApplicationErrors.BalanceOutcomeType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BalanceObservability {

	private final Map<BalanceOutcomeType, Counter> reserveOutcomeCounters;

	public BalanceObservability(MeterRegistry meterRegistry) {
		this.reserveOutcomeCounters = new EnumMap<>(BalanceOutcomeType.class);
		for (BalanceOutcomeType outcomeType : BalanceOutcomeType.values()) {
			reserveOutcomeCounters.put(outcomeType,
				Counter.builder("balance.reserve.outcome")
					.tag("outcome", outcomeType.name().toLowerCase())
					.register(meterRegistry));
		}
	}

	public void recordReserveOutcome(BalanceOutcomeType outcomeType) {
		reserveOutcomeCounters.get(outcomeType).increment();
	}
}
