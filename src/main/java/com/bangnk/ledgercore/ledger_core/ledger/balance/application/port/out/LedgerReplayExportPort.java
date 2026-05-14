package com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out;

import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.MoneyAmount;
import java.time.Instant;
import java.util.List;

public interface LedgerReplayExportPort {

	ReplayBatch export(ReplayCursor cursor, int limit);

	record ReplayCursor(long ledgerSequence, String checkpointToken) {
	}

	record ReplayBatch(List<ReplayRecord> records, ReplayCursor nextCursor, boolean complete) {
		public ReplayBatch {
			records = List.copyOf(records);
		}
	}

	record ReplayRecord(
			long ledgerSequence,
			String ledgerTransactionId,
			String ledgerEntryId,
			AccountId accountId,
			CurrencyCode currency,
			BalanceDirection direction,
			MoneyAmount amount,
			Instant postedAt
	) {
	}
}
