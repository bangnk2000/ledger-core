package com.bangnk.ledgercore.ledger_core.ledger.balance.application;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.LedgerReplayExportPort.ReplayRecord;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BalanceReplayOrderingService {

	private static final Comparator<ReplayRecord> DETERMINISTIC_REPLAY_ORDER =
		Comparator.comparingLong(ReplayRecord::ledgerSequence)
			.thenComparing(ReplayRecord::ledgerTransactionId, Comparator.naturalOrder())
			.thenComparing(ReplayRecord::ledgerEntryId, Comparator.naturalOrder());

	public List<ReplayRecord> orderDeterministically(List<ReplayRecord> records) {
		return records.stream()
			.sorted(DETERMINISTIC_REPLAY_ORDER)
			.toList();
	}
}
