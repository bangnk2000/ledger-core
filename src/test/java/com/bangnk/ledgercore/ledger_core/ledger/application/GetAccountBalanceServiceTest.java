package com.bangnk.ledgercore.ledger_core.ledger.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.AuditEventPublisher;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.BalanceSnapshot;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.LedgerEntryRepository;
import com.bangnk.ledgercore.ledger_core.ledger.application.query.AccountBalance;
import com.bangnk.ledgercore.ledger_core.ledger.application.query.GetAccountBalanceService;
import com.bangnk.ledgercore.ledger_core.ledger.config.LedgerObservabilityConfig.LedgerMetrics;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAccountBalanceServiceTest {

	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-12T00:00:00Z"), ZoneOffset.UTC);

	@Mock
	private LedgerEntryRepository entryRepository;

	@Mock
	private AuditEventPublisher auditEventPublisher;

	@Test
	void interpretsDebitEntriesAsPositiveBalance() {
		GetAccountBalanceService service = new GetAccountBalanceService(
			entryRepository,
			auditEventPublisher,
			new LedgerMetrics(new SimpleMeterRegistry()),
			CLOCK);
		when(entryRepository.summarizePostedBalance(new AccountId("cash"), "USD"))
			.thenReturn(new BalanceSnapshot(new BigDecimal("150.0000"), new BigDecimal("40.0000"), 3L));

		AccountBalance balance = service.getBalance(new AccountId("cash"), "USD");

		assertThat(balance.balance()).isEqualByComparingTo("110.0000");
		assertThat(balance.entryCount()).isEqualTo(3L);
		assertThat(balance.calculatedAt()).isEqualTo(Instant.now(CLOCK));
		verify(auditEventPublisher).publish(argThat(event ->
			event.eventType().equals("BALANCE_CALCULATED")
				&& event.safeDetails().get("currency").equals("USD")
				&& event.safeDetails().get("entryCount").equals(3L)));
	}

	@Test
	void interpretsCreditEntriesAsNegativeBalance() {
		GetAccountBalanceService service = new GetAccountBalanceService(
			entryRepository,
			auditEventPublisher,
			new LedgerMetrics(new SimpleMeterRegistry()),
			CLOCK);
		when(entryRepository.summarizePostedBalance(new AccountId("payable"), "USD"))
			.thenReturn(new BalanceSnapshot(new BigDecimal("20.0000"), new BigDecimal("75.0000"), 2L));

		AccountBalance balance = service.getBalance(new AccountId("payable"), "USD");

		assertThat(balance.balance()).isEqualByComparingTo("-55.0000");
		assertThat(balance.entryCount()).isEqualTo(2L);
	}
}
