package com.bangnk.ledgercore.ledger_core.idempotency.application;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.ReplayOutcome;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.ClaimOwner;

public record IdempotencyDecision(
    IdempotencyDecisionTypes decisionType,
    ClaimOwner claimOwner,
    ReplayOutcome replayOutcome,
    String message
) {
    public static IdempotencyDecision firstExecution(ClaimOwner claimOwner) {
        return new IdempotencyDecision(IdempotencyDecisionTypes.FIRST_EXECUTION, claimOwner, null, "First execution allowed");
    }

    public static IdempotencyDecision replay(ReplayOutcome outcome) {
        return new IdempotencyDecision(IdempotencyDecisionTypes.REPLAY, null, outcome, "Replaying previous outcome");
    }

    public static IdempotencyDecision duplicateInProgress() {
        return new IdempotencyDecision(IdempotencyDecisionTypes.DUPLICATE_IN_PROGRESS, null, null, "Operation already in progress");
    }

    public static IdempotencyDecision conflict(String reason) {
        return new IdempotencyDecision(IdempotencyDecisionTypes.CONFLICT, null, null, reason);
    }

    public static IdempotencyDecision expiredKey() {
        return new IdempotencyDecision(IdempotencyDecisionTypes.EXPIRED_KEY, null, null, "Key has expired");
    }

    public static IdempotencyDecision indeterminate(ReplayOutcome outcome) {
        return new IdempotencyDecision(IdempotencyDecisionTypes.INDETERMINATE, null, outcome, "Previous execution was indeterminate");
    }
}
