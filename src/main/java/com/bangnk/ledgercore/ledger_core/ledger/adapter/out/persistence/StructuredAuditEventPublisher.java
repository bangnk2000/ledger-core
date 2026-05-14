package com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.AuditEventPublisher;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StructuredAuditEventPublisher implements AuditEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(StructuredAuditEventPublisher.class);

	@Override
	public void publish(AuditEvent event) {
		log.info("ledger_audit eventType={} requestScope={} requestId={} transactionId={} actorType={} actorId={} details={}",
			event.eventType(),
			event.requestIdentity().requesterScope(),
			event.requestIdentity().requestId(),
			event.transactionId(),
			event.actor().actorType(),
			event.actor().actorId(),
			event.safeDetails());
	}
}
