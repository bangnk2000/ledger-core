package com.bangnk.ledgercore.ledger_core.balance.domain.model;

import com.bangnk.ledgercore.ledger_core.balance.application.port.out.FundsReservationRepositoryPort.FundsReservationRecord;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReservationStatus;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
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

	public FundsReservation confirm(String ledgerTxnId, String confirmationRef, Instant now) {
		if (status != ReservationStatus.ACTIVE && status != ReservationStatus.RECOVERY_PENDING) {
			throw new IllegalArgumentException("Reservation cannot be confirmed from status " + status);
		}
		return new FundsReservation(
			reservationId,
			requestIdentity,
			accountId,
			currency,
			direction,
			amount,
			businessReference,
			ReservationStatus.CONFIRMED,
			expiresAt,
			ledgerTxnId,
			confirmationRef,
			createdAt,
			now);
	}

	public FundsReservation release(ReservationStatus nextStatus, Instant now) {
		if (nextStatus != ReservationStatus.CANCELLED
				&& nextStatus != ReservationStatus.EXPIRED
				&& nextStatus != ReservationStatus.RECOVERY_PENDING) {
			throw new IllegalArgumentException("Unsupported release status " + nextStatus);
		}
		if (status != ReservationStatus.ACTIVE && status != ReservationStatus.RECOVERY_PENDING) {
			throw new IllegalArgumentException("Reservation cannot be released from status " + status);
		}
		return new FundsReservation(
			reservationId,
			requestIdentity,
			accountId,
			currency,
			direction,
			amount,
			businessReference,
			nextStatus,
			expiresAt,
			ledgerTransactionId,
			confirmationReference,
			createdAt,
			now);
	}
}
