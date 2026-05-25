package com.bangnk.ledgercore.ledger_core.balance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceStateRepositoryPort.BalanceStateKey;
import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceStateRepositoryPort.BalanceStateRecord;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReconciliationStatus;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceSnapshot;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceSnapshot.ConsistencyMode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BalanceSnapshotTest {

	@Test
	void derivesAvailableBalanceAndFreshnessFromState() {
		BalanceStateRecord state = new BalanceStateRecord(
			new BalanceStateKey(new AccountId("acc-snp-1"), new CurrencyCode("USD")),
			new MoneyAmount(new BigDecimal("10.0000")),
			new MoneyAmount(new BigDecimal("2.5000")),
			new MoneyAmount(new BigDecimal("1.0000")),
			new MoneyAmount(new BigDecimal("0.5000")),
			3L,
			9L,
			4L,
			ReconciliationStatus.HEALTHY,
			Instant.parse("2026-05-16T10:00:00Z"));

		BalanceSnapshot snapshot = BalanceSnapshot.fromState(state, ConsistencyMode.STRONG, Instant.parse("2026-05-16T10:00:01Z"));

		assertThat(snapshot.availableBalance().value()).isEqualByComparingTo("7.0000");
		assertThat(snapshot.snapshotVersion()).isEqualTo(3L);
		assertThat(snapshot.asOfSequence()).isEqualTo(9L);
		assertThat(snapshot.consistencyMode()).isEqualTo(ConsistencyMode.STRONG);
	}
}
