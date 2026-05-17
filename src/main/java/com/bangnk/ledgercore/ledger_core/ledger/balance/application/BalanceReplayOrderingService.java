package com.bangnk.ledgercore.ledger_core.ledger.balance.application;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.LedgerReplayExportPort.ReplayRecord;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BalanceReplayOrderingService {

	public List<ReplayRecord> orderDeterministically(List<ReplayRecord> records) {
		return records.stream()
			.sorted(Comparator.comparingLong(ReplayRecord::ledgerSequence)
				.thenComparing(ReplayRecord::ledgerTransactionId)
				.thenComparing(ReplayRecord::ledgerEntryId))
			.toList();
	}
}
