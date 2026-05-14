package com.bangnk.ledgercore.ledger_core.ledger.application.command;

import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerEntry;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction.LedgerEntryDraft;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LineId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.Money;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LedgerCorrectionFactory {

	public List<LedgerEntryDraft> reverse(List<LedgerEntry> originalEntries) {
		return originalEntries.stream()
			.map(entry -> new LedgerEntryDraft(
				new LineId("reversal-" + entry.lineId().value()),
				entry.accountId(),
				entry.direction() == Direction.DEBIT ? Direction.CREDIT : Direction.DEBIT,
				new Money(entry.money().amount(), entry.money().currency()),
				entry.metadata()))
			.toList();
	}
}
