package com.bangnk.ledgercore.ledger_core.audit.application.port.in;

import com.bangnk.ledgercore.ledger_core.audit.application.AuditQueryCriteria;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.InvestigationView;

public interface AuditInvestigationQueryUseCase {
    InvestigationView query(AuditQueryCriteria criteria);
}
