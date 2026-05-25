package com.bangnk.ledgercore.ledger_core.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.balance.adapter.out.persistence.BalanceStateJpaEntity.BalanceStateId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface BalanceStateJpaRepository extends JpaRepository<BalanceStateJpaEntity, BalanceStateId> {
	Optional<BalanceStateJpaEntity> findByAccountIdAndCurrency(String accountId, String currency);

	@Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from BalanceStateJpaEntity s where s.accountId = :accountId and s.currency = :currency")
	Optional<BalanceStateJpaEntity> findByAccountIdAndCurrencyForUpdate(@Param("accountId") String accountId, @Param("currency") String currency);

	@Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from BalanceStateJpaEntity s where concat(s.accountId, '|', s.currency) in :keys order by s.accountId asc, s.currency asc")
	List<BalanceStateJpaEntity> lockDeterministic(@Param("keys") List<String> keys);
}

interface FundsReservationJpaRepository extends JpaRepository<FundsReservationJpaEntity, UUID> {
	Optional<FundsReservationJpaEntity> findByRequesterScopeAndRequestId(String requesterScope, String requestId);

	@Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from FundsReservationJpaEntity r where r.reservationId = :reservationId")
	Optional<FundsReservationJpaEntity> findByIdForUpdate(@Param("reservationId") UUID reservationId);

	List<FundsReservationJpaEntity> findByStatusAndExpiresAtLessThanEqual(
		BalanceEnums.ReservationStatus status,
		java.time.Instant expiresAt);
}

interface BalanceIdempotencyJpaRepository extends JpaRepository<BalanceIdempotencyJpaEntity, BalanceIdempotencyJpaEntity.BalanceIdempotencyKey> {
}

interface BalanceSnapshotJpaRepository extends JpaRepository<BalanceSnapshotJpaEntity, UUID> {
	Optional<BalanceSnapshotJpaEntity> findTopByAccountIdAndCurrencyOrderBySnapshotVersionDesc(String accountId, String currency);
}

interface BalanceRebuildJobJpaRepository extends JpaRepository<BalanceRebuildJobJpaEntity, UUID> {
}

interface BalanceRebuildCheckpointJpaRepository extends JpaRepository<BalanceRebuildCheckpointJpaEntity, UUID> {
	Optional<BalanceRebuildCheckpointJpaEntity> findTopByJobIdOrderByCreatedAtDesc(UUID jobId);
}

interface BalanceReconciliationRecordJpaRepository extends JpaRepository<BalanceReconciliationRecordJpaEntity, UUID> {
}
