package com.bangnk.ledgercore.ledger_core.balance.domain.model;

public final class BalanceEnums {

	private BalanceEnums() {
	}

	public enum BalanceDirection {
		DEBIT,
		CREDIT
	}

	public enum ReconciliationStatus {
		HEALTHY,
		DRIFT_DETECTED,
		REBUILDING,
		RECOVERY_REQUIRED
	}

	public enum ReservationStatus {
		ACTIVE,
		CONFIRMED,
		EXPIRED,
		CANCELLED,
		RECOVERY_PENDING,
		FAILED_RECOVERY
	}

	public enum BalanceMutationType {
		RESERVE,
		CONFIRM,
		CANCEL,
		EXPIRE,
		RELEASE,
		RECOVER
	}

	public enum BalanceActorType {
		USER,
		SYSTEM,
		SERVICE
	}

	public enum ConsistencyMode {
		STRONG,
		DERIVED
	}

	public enum RebuildJobStatus {
		PENDING,
		RUNNING,
		SUCCEEDED,
		FAILED,
		CANCELLED
	}

	public enum ReconciliationSeverity {
		INFO,
		WARNING,
		CRITICAL
	}

	public enum ReconciliationRecordStatus {
		OPEN,
		ACKNOWLEDGED,
		REPAIRED,
		FALSE_POSITIVE
	}
}
