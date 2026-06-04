package com.bangnk.ledgercore.ledger_core.audit.application.port.in;

import com.bangnk.ledgercore.ledger_core.audit.application.AuditCaptureCommand;
import com.bangnk.ledgercore.ledger_core.audit.application.AuditDecisionTypes.CaptureDecisionType;
import java.util.UUID;

public interface AuditCaptureUseCase {
    CaptureResult capture(AuditCaptureCommand command);

    record CaptureResult(UUID eventId, CaptureDecisionType decisionType) {}
}
