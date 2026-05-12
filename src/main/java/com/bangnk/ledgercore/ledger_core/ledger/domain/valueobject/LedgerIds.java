package com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class LedgerIds {

	private LedgerIds() {
	}

	public record AccountId(String value) {
		public AccountId {
			validate(value, "accountId");
		}
	}

	public record LedgerTransactionId(UUID value) {
		public LedgerTransactionId {
			Objects.requireNonNull(value, "transactionId must not be null");
		}

		public static LedgerTransactionId newId() {
			return new LedgerTransactionId(UUID.randomUUID());
		}
	}

	public record LedgerEntryId(UUID value) {
		public LedgerEntryId {
			Objects.requireNonNull(value, "entryId must not be null");
		}

		public static LedgerEntryId newId() {
			return new LedgerEntryId(UUID.randomUUID());
		}
	}

	public record LineId(String value) {
		public LineId {
			validate(value, "lineId");
		}
	}

	private static void validate(String value, String field) {
		Objects.requireNonNull(value, field + " must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException(field + " must not be blank");
		}
	}
}
