package com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerJpaEntities.LedgerEntryJpaEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryJpaEntity, UUID> {

	List<LedgerEntryJpaEntity> findByTransactionId(UUID transactionId);

	@Query("""
		select new com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerEntryJpaRepository$BalanceProjection(
			coalesce(sum(case when e.direction = com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction.DEBIT then e.amount else 0 end), 0),
			coalesce(sum(case when e.direction = com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction.CREDIT then e.amount else 0 end), 0),
			count(e)
		)
		from LedgerJpaEntities$LedgerEntryJpaEntity e
		where e.accountId = :accountId
		  and e.currency = :currency
		  and exists (
		  	select 1
		  	from LedgerJpaEntities$LedgerTransactionJpaEntity t
		  	where t.id = e.transactionId
		  	  and t.status = com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.TransactionStatus.POSTED
		  )
		""")
	BalanceProjection calculateBalance(@Param("accountId") String accountId, @Param("currency") String currency);

	record BalanceProjection(BigDecimal debitTotal, BigDecimal creditTotal, long entryCount) {
	}
}
