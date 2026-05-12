package com.bangnk.ledgercore.ledger_core.ledger.application.query;

import com.bangnk.ledgercore.ledger_core.ledger.application.port.in.LedgerUseCases.GetAccountBalanceQuery;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.AuditEventPublisher;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.BalanceSnapshot;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.LedgerEntryRepository;
import com.bangnk.ledgercore.ledger_core.ledger.config.LedgerObservabilityConfig.LedgerMetrics;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAccountBalanceService implements GetAccountBalanceQuery {

	private final LedgerEntryRepository entryRepository;
	private final AuditEventPublisher auditEventPublisher;
	private final Clock clock;
	private final Counter successCounter;
	private final Timer balanceTimer;

	public GetAccountBalanceService(LedgerEntryRepository entryRepository, AuditEventPublisher auditEventPublisher, LedgerMetrics ledgerMetrics, Clock clock) {
		this.entryRepository = entryRepository;
		this.auditEventPublisher = auditEventPublisher;
		this.clock = clock;
		this.successCounter = ledgerMetrics.balanceOutcome("success");
		this.balanceTimer = ledgerMetrics.balanceTimer();
	}

	@Override
	@Transactional(readOnly = true)
	public AccountBalance getBalance(AccountId accountId, String currency) {
		Timer.Sample sample = Timer.start();
		try {
			BalanceSnapshot snapshot = entryRepository.summarizePostedBalance(accountId, currency);
			BigDecimal debitTotal = snapshot == null || snapshot.debitTotal() == null ? BigDecimal.ZERO.setScale(4) : snapshot.debitTotal();
			BigDecimal creditTotal = snapshot == null || snapshot.creditTotal() == null ? BigDecimal.ZERO.setScale(4) : snapshot.creditTotal();
			AccountBalance balance = new AccountBalance(
				accountId.value(),
				debitTotal.subtract(creditTotal),
				currency,
				Instant.now(clock),
				snapshot == null ? 0L : snapshot.entryCount());
			successCounter.increment();
			auditEventPublisher.publish(new AuditTrace.AuditEvent(
				"BALANCE_CALCULATED",
				new RequestIdentity("ledger-balance", accountId.value()),
				null,
				new AuditTrace.Actor("ledger-balance-service", ActorType.SYSTEM),
				null,
				null,
				Instant.now(clock),
				Map.of("entryCount", balance.entryCount(), "currency", balance.currency())));
			return balance;
		} finally {
			sample.stop(balanceTimer);
		}
	}
}
