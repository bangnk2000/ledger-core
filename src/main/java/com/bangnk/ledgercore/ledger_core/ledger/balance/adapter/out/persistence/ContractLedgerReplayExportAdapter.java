package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.LedgerReplayExportPort;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ContractLedgerReplayExportAdapter implements LedgerReplayExportPort {

	@Override
	public ReplayBatch export(ReplayCursor cursor, int limit) {
		return new ReplayBatch(List.of(), new ReplayCursor(cursor.ledgerSequence(), cursor.checkpointToken()), true);
	}
}
