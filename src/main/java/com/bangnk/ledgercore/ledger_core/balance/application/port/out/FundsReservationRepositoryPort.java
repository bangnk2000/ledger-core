package com.bangnk.ledgercore.ledger_core.balance.application.port.out;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.ReservationStatus;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FundsReservationRepositoryPort {

	Optional<FundsReservationRecord> findById(UUID reservationId);

	Optional<FundsReservationRecord> findByIdForUpdate(UUID reservationId);

	Optional<FundsReservationRecord> findByRequestIdentity(RequestIdentity requestIdentity);

	List<FundsReservationRecord> findExpiredActive(Instant asOf);

	FundsReservationRecord save(FundsReservationRecord reservation);

	record FundsReservationRecord(
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
	}
}
