package com.bangnk.ledgercore.ledger_core.ledger.balance.application;

import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.RequestIdentity;
import java.time.Instant;

public final class BalanceApplicationErrors {

	private BalanceApplicationErrors() {
	}

	public enum BalanceOutcomeType {
		ACCEPTED,
		DUPLICATE,
		REJECTED,
		CONFLICT,
		LOCKED,
		FAILED
	}

	public record BalanceOutcome(
			BalanceOutcomeType outcome,
			String code,
			String message,
			RequestIdentity requestIdentity,
			Instant occurredAt
	) {
		public static BalanceOutcome accepted(String code, String message, RequestIdentity requestIdentity) {
			return new BalanceOutcome(BalanceOutcomeType.ACCEPTED, code, message, requestIdentity, Instant.now());
		}

		public static BalanceOutcome duplicate(String code, String message, RequestIdentity requestIdentity) {
			return new BalanceOutcome(BalanceOutcomeType.DUPLICATE, code, message, requestIdentity, Instant.now());
		}

		public static BalanceOutcome rejected(String code, String message, RequestIdentity requestIdentity) {
			return new BalanceOutcome(BalanceOutcomeType.REJECTED, code, message, requestIdentity, Instant.now());
		}

		public static BalanceOutcome conflict(String code, String message, RequestIdentity requestIdentity) {
			return new BalanceOutcome(BalanceOutcomeType.CONFLICT, code, message, requestIdentity, Instant.now());
		}

		public static BalanceOutcome locked(String code, String message, RequestIdentity requestIdentity) {
			return new BalanceOutcome(BalanceOutcomeType.LOCKED, code, message, requestIdentity, Instant.now());
		}

		public static BalanceOutcome failed(String code, String message, RequestIdentity requestIdentity) {
			return new BalanceOutcome(BalanceOutcomeType.FAILED, code, message, requestIdentity, Instant.now());
		}
	}

	public static class BalanceDomainException extends RuntimeException {
		private final BalanceOutcomeType outcome;
		private final String code;

		public BalanceDomainException(BalanceOutcomeType outcome, String code, String message) {
			super(message);
			this.outcome = outcome;
			this.code = code;
		}

		public BalanceOutcomeType getOutcome() {
			return outcome;
		}

		public String getCode() {
			return code;
		}
	}

	public static BalanceDomainException invalidRequest(String code, String message) {
		return new BalanceDomainException(BalanceOutcomeType.REJECTED, code, message);
	}

	public static BalanceDomainException conflict(String code, String message) {
		return new BalanceDomainException(BalanceOutcomeType.CONFLICT, code, message);
	}

	public static BalanceDomainException locked(String code, String message) {
		return new BalanceDomainException(BalanceOutcomeType.LOCKED, code, message);
	}
}
