package com.bangnk.ledgercore.ledger_core.ledger.application.command;

import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction.LedgerEntryDraft;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record PostLedgerTransactionCommand(
		RequestIdentity requestIdentity,
		AuditTrace auditTrace,
		String businessReference,
		String description,
		Map<String, Object> metadata,
		List<LedgerEntryDraft> entries
) {
	public PostLedgerTransactionCommand {
		Objects.requireNonNull(requestIdentity, "requestIdentity must not be null");
		Objects.requireNonNull(auditTrace, "auditTrace must not be null");
		metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
		entries = entries == null ? List.of() : List.copyOf(entries);
	}
}
