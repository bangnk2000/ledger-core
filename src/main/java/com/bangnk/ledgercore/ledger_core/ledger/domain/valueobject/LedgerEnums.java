package com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject;

public final class LedgerEnums {

	private LedgerEnums() {
	}

	public enum Direction {
		DEBIT,
		CREDIT
	}

	public enum TransactionStatus {
		POSTED,
		REJECTED,
		FAILED
	}

	public enum ActorType {
		USER,
		SYSTEM,
		SERVICE
	}

	public enum PostingOutcomeType {
		ACCEPTED,
		REJECTED,
		DUPLICATE,
		CONFLICT,
		FAILED
	}
}
