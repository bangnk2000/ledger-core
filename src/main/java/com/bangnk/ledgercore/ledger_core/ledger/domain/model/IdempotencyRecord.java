package com.bangnk.ledgercore.ledger_core.ledger.domain.model;

import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.PostingOutcomeType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity.RequestHash;
import java.time.Instant;
import java.util.Objects;

public record IdempotencyRecord(
		RequestIdentity requestIdentity,
		RequestHash requestHash,
		PostingOutcomeType outcome,
		String transactionId,
		String responseCode,
		Instant createdAt,
		Instant lastSeenAt
) {
	public IdempotencyRecord {
		Objects.requireNonNull(requestIdentity, "requestIdentity must not be null");
		Objects.requireNonNull(requestHash, "requestHash must not be null");
		Objects.requireNonNull(outcome, "outcome must not be null");
		Objects.requireNonNull(responseCode, "responseCode must not be null");
		Objects.requireNonNull(createdAt, "createdAt must not be null");
		Objects.requireNonNull(lastSeenAt, "lastSeenAt must not be null");
	}

	public IdempotencyRecord touch(Instant now) {
		return new IdempotencyRecord(requestIdentity, requestHash, outcome, transactionId, responseCode, createdAt, now);
	}
}
