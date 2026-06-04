package com.bangnk.ledgercore.ledger_core.audit.application.port.in;

import com.bangnk.ledgercore.ledger_core.audit.application.AuditDecisionTypes.IntegrityVerificationDecisionType;
import java.time.Instant;
import java.util.UUID;

public interface AuditIntegrityVerificationUseCase {
    IntegrityVerificationResult verify(UUID eventId, Instant verifiedAt);

    record IntegrityVerificationResult(UUID eventId, IntegrityVerificationDecisionType decisionType) {}
}
