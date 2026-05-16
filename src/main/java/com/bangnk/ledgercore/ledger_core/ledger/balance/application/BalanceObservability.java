package com.bangnk.ledgercore.ledger_core.ledger.balance.application;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.BalanceApplicationErrors.BalanceOutcomeType;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceSnapshot.ConsistencyMode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BalanceObservability {

	private final Map<BalanceOutcomeType, Counter> reserveOutcomeCounters;
	private final Map<BalanceOutcomeType, Counter> lifecycleOutcomeCounters;
	private final Map<ConsistencyMode, Counter> balanceReadCounters;
	private final Counter rebuildProcessedCounter;
	private final Counter reconciliationDiscrepancyCounter;
	private final Counter contentionCounter;
	private final Counter retryCounter;
	private final Counter duplicateCounter;
	private final Counter failClosedCounter;

	public BalanceObservability(MeterRegistry meterRegistry) {
		this.reserveOutcomeCounters = new EnumMap<>(BalanceOutcomeType.class);
		for (BalanceOutcomeType outcomeType : BalanceOutcomeType.values()) {
			reserveOutcomeCounters.put(outcomeType,
				Counter.builder("balance.reserve.outcome")
					.tag("outcome", outcomeType.name().toLowerCase())
					.register(meterRegistry));
		}
		this.lifecycleOutcomeCounters = new EnumMap<>(BalanceOutcomeType.class);
		for (BalanceOutcomeType outcomeType : BalanceOutcomeType.values()) {
			lifecycleOutcomeCounters.put(outcomeType,
				Counter.builder("balance.lifecycle.outcome")
					.tag("outcome", outcomeType.name().toLowerCase())
					.register(meterRegistry));
		}
		this.balanceReadCounters = new EnumMap<>(ConsistencyMode.class);
		for (ConsistencyMode mode : ConsistencyMode.values()) {
			balanceReadCounters.put(mode,
				Counter.builder("balance.read.consistency")
					.tag("consistency", mode.name().toLowerCase())
					.register(meterRegistry));
		}
		this.rebuildProcessedCounter = Counter.builder("balance.rebuild.processed.records")
			.register(meterRegistry);
		this.reconciliationDiscrepancyCounter = Counter.builder("balance.reconciliation.discrepancy")
			.tag("severity", "all")
			.register(meterRegistry);
		this.contentionCounter = Counter.builder("balance.write.contention")
			.register(meterRegistry);
		this.retryCounter = Counter.builder("balance.write.retry")
			.register(meterRegistry);
		this.duplicateCounter = Counter.builder("balance.write.duplicate")
			.register(meterRegistry);
		this.failClosedCounter = Counter.builder("balance.write.fail.closed")
			.register(meterRegistry);
	}

	public void recordReserveOutcome(BalanceOutcomeType outcomeType) {
		reserveOutcomeCounters.get(outcomeType).increment();
	}

	public void recordBalanceRead(ConsistencyMode consistencyMode) {
		balanceReadCounters.get(consistencyMode).increment();
	}

	public void recordLifecycleOutcome(BalanceOutcomeType outcomeType) {
		lifecycleOutcomeCounters.get(outcomeType).increment();
	}

	public void recordRebuildProgress(long processedRecords) {
		rebuildProcessedCounter.increment(processedRecords);
	}

	public void recordReconciliationDiscrepancy(String severity) {
		reconciliationDiscrepancyCounter.increment();
	}

	public void recordContention() {
		contentionCounter.increment();
	}

	public void recordRetry(int attempt) {
		retryCounter.increment();
	}

	public void recordDuplicate() {
		duplicateCounter.increment();
	}

	public void recordFailClosed() {
		failClosedCounter.increment();
	}
}
