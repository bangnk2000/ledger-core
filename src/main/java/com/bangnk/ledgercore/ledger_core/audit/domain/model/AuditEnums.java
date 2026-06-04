package com.bangnk.ledgercore.ledger_core.audit.domain.model;

public final class AuditEnums {
    private AuditEnums() {}

    public enum AuditEventType {
        INTENT,
        STATE_TRANSITION,
        LEDGER_POSTING,
        APPROVAL,
        CORRECTION,
        POLICY_DECISION,
        INTEGRATION_HANDOFF,
        PUBLICATION_OUTCOME,
        RETENTION_OUTCOME
    }

    public enum ActorType {
        USER,
        SYSTEM,
        SCHEDULED_JOB,
        SERVICE
    }

    public enum PresenceStatus {
        KNOWN,
        UNAVAILABLE,
        NOT_APPLICABLE
    }

    public enum ReferenceStatus {
        PRESENT,
        ABSENT
    }

    public enum PublicationResult {
        SUCCEEDED,
        DEFERRED,
        FAILED
    }

    public enum IntegrityVerificationStatus {
        UNVERIFIED,
        VERIFIED,
        FAILED
    }
}
