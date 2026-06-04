package com.bangnk.ledgercore.ledger_core.audit.application.port.in;

import com.bangnk.ledgercore.ledger_core.audit.application.AuditDecisionTypes.PublicationRecoveryDecisionType;
import java.time.Instant;

public interface AuditPublicationRecoveryUseCase {
    PublicationRecoveryResult recoverPending(Instant asOf);

    record PublicationRecoveryResult(int attemptedCount, PublicationRecoveryDecisionType decisionType) {}
}
