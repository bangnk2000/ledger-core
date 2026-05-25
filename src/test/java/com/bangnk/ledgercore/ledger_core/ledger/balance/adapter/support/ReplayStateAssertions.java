package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

public final class ReplayStateAssertions {

	private ReplayStateAssertions() {
	}

	public static void assertBaselineMatchesRefactor(
		Map<String, List<Map<String, Object>>> baselineState,
		Map<String, List<Map<String, Object>>> refactorState
	) {
		assertThat(refactorState)
			.as("Replay state must remain stable between baseline and refactor execution")
			.isEqualTo(baselineState);
	}
}
