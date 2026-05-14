package com.bangnk.ledgercore.ledger_core.ledger.application;

import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.PostingOutcomeType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import java.time.Instant;

public record PostingOutcome(
		PostingOutcomeType outcome,
		String code,
		String message,
		RequestIdentity requestIdentity,
		String transactionId,
		Instant postedAt,
		AuditTrace auditTrace
) {
	public static PostingOutcome accepted(String code, String message, RequestIdentity requestIdentity, String transactionId, Instant postedAt, AuditTrace auditTrace) {
		return new PostingOutcome(PostingOutcomeType.ACCEPTED, code, message, requestIdentity, transactionId, postedAt, auditTrace);
	}

	public static PostingOutcome duplicate(String code, String message, RequestIdentity requestIdentity, String transactionId, Instant postedAt, AuditTrace auditTrace) {
		return new PostingOutcome(PostingOutcomeType.DUPLICATE, code, message, requestIdentity, transactionId, postedAt, auditTrace);
	}

	public static PostingOutcome rejected(String code, String message, RequestIdentity requestIdentity, String transactionId, AuditTrace auditTrace) {
		return new PostingOutcome(PostingOutcomeType.REJECTED, code, message, requestIdentity, transactionId, null, auditTrace);
	}

	public static PostingOutcome conflict(String code, String message, RequestIdentity requestIdentity, AuditTrace auditTrace) {
		return new PostingOutcome(PostingOutcomeType.CONFLICT, code, message, requestIdentity, null, null, auditTrace);
	}

	public static PostingOutcome failed(String code, String message, RequestIdentity requestIdentity, AuditTrace auditTrace) {
		return new PostingOutcome(PostingOutcomeType.FAILED, code, message, requestIdentity, null, null, auditTrace);
	}

	public boolean isAcceptedLike() {
		return outcome == PostingOutcomeType.ACCEPTED || outcome == PostingOutcomeType.DUPLICATE;
	}

	public static class LedgerDomainException extends RuntimeException {
		private final String code;

		public LedgerDomainException(String code, String message) {
			super(message);
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}
}
