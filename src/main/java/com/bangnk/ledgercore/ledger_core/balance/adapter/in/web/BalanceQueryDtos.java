package com.bangnk.ledgercore.ledger_core.balance.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceSnapshot;
import java.time.Instant;

public final class BalanceQueryDtos {

	private BalanceQueryDtos() {
	}

	public record CurrentBalanceViewDto(
			String accountId,
			String currency,
			String ledgerBalance,
			String lockedAmount,
			String pendingDebitAmount,
			String pendingCreditAmount,
			String availableBalance,
			long snapshotVersion,
			String asOfSequence,
			Instant asOfTime,
			String consistencyMode,
			String reconciliationStatus
	) {
		static CurrentBalanceViewDto from(BalanceSnapshot snapshot) {
			return new CurrentBalanceViewDto(
				snapshot.accountId().value(),
				snapshot.currency().value(),
				snapshot.ledgerBalance().value().toPlainString(),
				snapshot.lockedAmount().value().toPlainString(),
				snapshot.pendingDebitAmount().value().toPlainString(),
				snapshot.pendingCreditAmount().value().toPlainString(),
				snapshot.availableBalance().value().toPlainString(),
				snapshot.snapshotVersion(),
				String.valueOf(snapshot.asOfSequence()),
				snapshot.asOfTime(),
				snapshot.consistencyMode().name(),
				snapshot.reconciliationStatus().name());
		}
	}
}
