package com.bangnk.ledgercore.ledger_core.audit.application;

public final class AuditDecisionTypes {
    private AuditDecisionTypes() {}

    public enum CaptureDecisionType {
        CAPTURED,
        CAPTURED_WITH_DEFERRED_PUBLICATION
    }

    public enum PublicationRecoveryDecisionType {
        RETRIED,
        NO_PENDING_PUBLICATION
    }

    public enum IntegrityVerificationDecisionType {
        VERIFIED,
        FAILED
    }

    public enum RetentionDecisionType {
        ACTIVE_RETAINED,
        RESTRICTED_RETAINED,
        DISPOSED
    }
}
