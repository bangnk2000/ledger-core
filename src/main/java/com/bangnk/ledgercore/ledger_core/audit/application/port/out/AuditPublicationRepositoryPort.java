package com.bangnk.ledgercore.ledger_core.audit.application.port.out;

import com.bangnk.ledgercore.ledger_core.audit.domain.model.PublicationAttempt;
import java.time.Instant;
import java.util.List;

public interface AuditPublicationRepositoryPort {
    PublicationAttempt saveAttempt(PublicationAttempt attempt);

    List<PublicationAttempt> findPendingAttempts(Instant asOf);
}
