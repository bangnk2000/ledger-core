package com.bangnk.ledgercore.ledger_core.ledger.domain.model;

import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerEntryId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerTransactionId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LineId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.Money;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record LedgerEntry(
		LedgerEntryId id,
		LedgerTransactionId transactionId,
		LineId lineId,
		AccountId accountId,
		Direction direction,
		Money money,
		Map<String, Object> metadata,
		Instant postedAt
) {
	public LedgerEntry {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(transactionId, "transactionId must not be null");
		Objects.requireNonNull(lineId, "lineId must not be null");
		Objects.requireNonNull(accountId, "accountId must not be null");
		Objects.requireNonNull(direction, "direction must not be null");
		Objects.requireNonNull(money, "money must not be null");
		Objects.requireNonNull(postedAt, "postedAt must not be null");
		metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
	}
}
