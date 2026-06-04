package com.bangnk.ledgercore.ledger_core.audit.application.port.in;

import com.bangnk.ledgercore.ledger_core.audit.application.AuditDecisionTypes.RetentionDecisionType;
import java.time.Instant;

public interface AuditRetentionUseCase {
    RetentionResult processRetention(Instant asOf);

    record RetentionResult(int affectedEvents, RetentionDecisionType decisionType) {}
}
