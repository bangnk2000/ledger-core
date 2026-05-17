package com.bangnk.ledgercore.ledger_core.ledger.balance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.ReservationStatus;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.FundsReservation;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.RequestIdentity;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class FundsReservationTest {

	@Test
	void createsActiveReservationWithRequestIdentity() {
		RequestIdentity identity = new RequestIdentity("scope-1", "request-1");
		FundsReservation reservation = FundsReservation.createActive(
			identity,
			new AccountId("cash-1"),
			new CurrencyCode("USD"),
			BalanceDirection.DEBIT,
			new MoneyAmount(new BigDecimal("10.0000")),
			"invoice-1",
			Instant.parse("2026-05-14T12:00:00Z"),
			Instant.parse("2026-05-14T11:00:00Z"));

		assertThat(reservation.status()).isEqualTo(ReservationStatus.ACTIVE);
		assertThat(reservation.requestIdentity()).isEqualTo(identity);
		assertThat(reservation.reservationId()).isNotNull();
	}
}
