package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.FundsReservationRepositoryPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.RequestIdentity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaFundsReservationRepositoryAdapter implements FundsReservationRepositoryPort {

	private final FundsReservationJpaRepository repository;

	public JpaFundsReservationRepositoryAdapter(FundsReservationJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<FundsReservationRecord> findById(UUID reservationId) {
		return repository.findById(reservationId).map(this::toRecord);
	}

	@Override
	public Optional<FundsReservationRecord> findByIdForUpdate(UUID reservationId) {
		return repository.findByIdForUpdate(reservationId).map(this::toRecord);
	}

	@Override
	public Optional<FundsReservationRecord> findByRequestIdentity(RequestIdentity requestIdentity) {
		return repository.findByRequesterScopeAndRequestId(requestIdentity.requesterScope(), requestIdentity.requestId())
			.map(this::toRecord);
	}

	@Override
	public List<FundsReservationRecord> findExpiredActive(Instant asOf) {
		return repository.findByStatusAndExpiresAtLessThanEqual(
				com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.ReservationStatus.ACTIVE,
				asOf)
			.stream()
			.map(this::toRecord)
			.toList();
	}

	@Override
	public FundsReservationRecord save(FundsReservationRecord reservation) {
		repository.save(toEntity(reservation));
		return reservation;
	}

	private FundsReservationRecord toRecord(FundsReservationJpaEntity entity) {
		return new FundsReservationRecord(
			entity.getReservationId(),
			new RequestIdentity(entity.getRequesterScope(), entity.getRequestId()),
			new AccountId(entity.getAccountId()),
			new CurrencyCode(entity.getCurrency()),
			entity.getDirection(),
			new MoneyAmount(entity.getAmount()),
			entity.getBusinessReference(),
			entity.getStatus(),
			entity.getExpiresAt(),
			entity.getLedgerTransactionId(),
			entity.getConfirmationReference(),
			entity.getCreatedAt(),
			entity.getUpdatedAt());
	}

	private FundsReservationJpaEntity toEntity(FundsReservationRecord reservation) {
		return new FundsReservationJpaEntity(
			reservation.reservationId(),
			reservation.requestIdentity().requesterScope(),
			reservation.requestIdentity().requestId(),
			reservation.accountId().value(),
			reservation.currency().value(),
			reservation.direction(),
			reservation.amount().value(),
			reservation.businessReference(),
			reservation.status(),
			reservation.expiresAt(),
			reservation.ledgerTransactionId(),
			reservation.confirmationReference(),
			reservation.createdAt(),
			reservation.updatedAt());
	}
}
