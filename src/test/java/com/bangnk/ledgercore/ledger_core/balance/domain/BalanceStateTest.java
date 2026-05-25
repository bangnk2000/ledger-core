package com.bangnk.ledgercore.ledger_core.balance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceState;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BalanceStateTest {

	@Test
	void calculatesAvailableBalance() {
		BalanceState state = BalanceState.empty(new AccountId("acc-1"), new CurrencyCode("USD"));
		BalanceState reserved = state.reserve(BalanceDirection.DEBIT, new MoneyAmount(new BigDecimal("0.0000")), Instant.now());
		assertThat(reserved.availableBalance().value()).isEqualByComparingTo("0.0000");
	}

	@Test
	void preventsNegativeAvailableBalance() {
		BalanceState state = BalanceState.empty(new AccountId("acc-2"), new CurrencyCode("USD"));
		assertThatThrownBy(() -> state.reserve(BalanceDirection.DEBIT, new MoneyAmount(new BigDecimal("1.0000")), Instant.now()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Insufficient available balance");
	}
}
