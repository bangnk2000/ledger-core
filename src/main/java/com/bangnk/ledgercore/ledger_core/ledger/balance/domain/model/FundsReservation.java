package com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.FundsReservationRepositoryPort.FundsReservationRecord;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.ReservationStatus;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.RequestIdentity;
import java.time.Instant;
import java.util.UUID;

public record FundsReservation(
		UUID reservationId,
		RequestIdentity requestIdentity,
		AccountId accountId,
		CurrencyCode currency,
		BalanceDirection direction,
		MoneyAmount amount,
		String businessReference,
		ReservationStatus status,
		Instant expiresAt,
		String ledgerTransactionId,
		String confirmationReference,
		Instant createdAt,
		Instant updatedAt
) {
	public static FundsReservation createActive(
			RequestIdentity requestIdentity,
			AccountId accountId,
			CurrencyCode currency,
			BalanceDirection direction,
			MoneyAmount amount,
			String businessReference,
			Instant expiresAt,
			Instant now
	) {
		return new FundsReservation(
			UUID.randomUUID(),
			requestIdentity,
			accountId,
			currency,
			direction,
			amount,
			businessReference,
			ReservationStatus.ACTIVE,
			expiresAt,
			null,
			null,
			now,
			now);
	}

	public static FundsReservation fromRecord(FundsReservationRecord record) {
		return new FundsReservation(
			record.reservationId(),
			record.requestIdentity(),
			record.accountId(),
			record.currency(),
			record.direction(),
			record.amount(),
			record.businessReference(),
			record.status(),
			record.expiresAt(),
			record.ledgerTransactionId(),
			record.confirmationReference(),
			record.createdAt(),
			record.updatedAt());
	}

	public FundsReservationRecord toRecord() {
		return new FundsReservationRecord(
			reservationId,
			requestIdentity,
			accountId,
			currency,
			direction,
			amount,
			businessReference,
			status,
			expiresAt,
			ledgerTransactionId,
			confirmationReference,
			createdAt,
			updatedAt);
	}
}
