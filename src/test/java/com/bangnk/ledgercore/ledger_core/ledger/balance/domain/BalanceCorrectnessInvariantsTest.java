package com.bangnk.ledgercore.ledger_core.ledger.balance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceState;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.MoneyAmount;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BalanceCorrectnessInvariantsTest {

	@Test
	void debitReservationLifecyclePreservesConservation() {
		Instant now = Instant.parse("2026-05-19T01:00:00Z");
		BalanceState start = balance("100.0000", "0.0000", 7L);

		BalanceState reserved = start.reserve(BalanceDirection.DEBIT, amount("30.0000"), now.plusSeconds(1));
		assertThat(reserved.ledgerBalance().value()).isEqualByComparingTo("100.0000");
		assertThat(reserved.lockedAmount().value()).isEqualByComparingTo("30.0000");
		assertThat(reserved.availableBalance().value()).isEqualByComparingTo("70.0000");

		BalanceState confirmed = reserved.confirm(BalanceDirection.DEBIT, amount("30.0000"), now.plusSeconds(2));
		assertThat(confirmed.ledgerBalance().value()).isEqualByComparingTo("70.0000");
		assertThat(confirmed.lockedAmount().value()).isEqualByComparingTo("0.0000");
		assertThat(confirmed.availableBalance().value()).isEqualByComparingTo("70.0000");
	}

	@Test
	void releaseRestoresLockedFundsWithoutChangingLedger() {
		Instant now = Instant.parse("2026-05-19T01:00:00Z");
		BalanceState start = balance("100.0000", "20.0000", 4L);

		BalanceState released = start.release(BalanceDirection.DEBIT, amount("5.0000"), now.plusSeconds(1));
		assertThat(released.ledgerBalance().value()).isEqualByComparingTo("100.0000");
		assertThat(released.lockedAmount().value()).isEqualByComparingTo("15.0000");
		assertThat(released.availableBalance().value()).isEqualByComparingTo("85.0000");
	}

	@Test
	void rejectsInvalidTransitionsThatWouldProduceNegativeAmounts() {
		BalanceState start = balance("10.0000", "2.0000", 0L);

		assertThatThrownBy(() -> start.confirm(BalanceDirection.DEBIT, amount("3.0000"), Instant.now()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("invalid balance transition");

		assertThatThrownBy(() -> start.release(BalanceDirection.DEBIT, amount("3.0000"), Instant.now()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("invalid locked balance");
	}

	private static BalanceState balance(String ledger, String locked, long version) {
		return new BalanceState(
			new com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.BalanceStateRepositoryPort.BalanceStateKey(
				new AccountId("acc-invariant"),
				new CurrencyCode("USD")),
			amount(ledger),
			amount(locked),
			amount("0.0000"),
			amount("0.0000"),
			version,
			0L,
			0L,
			com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.ReconciliationStatus.HEALTHY,
			Instant.parse("2026-05-19T00:00:00Z"));
	}

	private static MoneyAmount amount(String value) {
		return new MoneyAmount(new BigDecimal(value));
	}
}
