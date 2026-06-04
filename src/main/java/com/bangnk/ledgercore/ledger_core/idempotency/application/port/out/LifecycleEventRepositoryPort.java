package com.bangnk.ledgercore.ledger_core.idempotency.application.port.out;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.LifecycleEvent;
import java.util.List;
import java.util.UUID;

public interface LifecycleEventRepositoryPort {
    LifecycleEvent save(LifecycleEvent event);
    List<LifecycleEvent> findByRecordId(UUID recordId);
}
