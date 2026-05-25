package com.bangnk.ledgercore.ledger_core.ledger.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.AuditEventPublisher;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.BalanceSnapshot;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.LedgerEntryRepository;
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
class GetAccountBalanceServiceCharacterizationTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-12T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private LedgerEntryRepository entryRepository;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    @Test
    void defaultsToZeroScaledValuesWhenSnapshotIsNull() {
        GetAccountBalanceService service = new GetAccountBalanceService(
            entryRepository,
            auditEventPublisher,
            new LedgerMetrics(new SimpleMeterRegistry()),
            CLOCK);
        when(entryRepository.summarizePostedBalance(new AccountId("cash"), "USD")).thenReturn(null);

        AccountBalance balance = service.getBalance(new AccountId("cash"), "USD");

        assertThat(balance.balance()).isEqualByComparingTo("0.0000");
        assertThat(balance.balance().scale()).isEqualTo(4);
        assertThat(balance.entryCount()).isZero();
    }

    @Test
    void defaultsNullTotalsToZeroWithStableScale() {
        GetAccountBalanceService service = new GetAccountBalanceService(
            entryRepository,
            auditEventPublisher,
            new LedgerMetrics(new SimpleMeterRegistry()),
            CLOCK);
        when(entryRepository.summarizePostedBalance(new AccountId("cash"), "USD"))
            .thenReturn(new BalanceSnapshot(null, new BigDecimal("2.5000"), 7L));

        AccountBalance balance = service.getBalance(new AccountId("cash"), "USD");

        assertThat(balance.balance()).isEqualByComparingTo("-2.5000");
        assertThat(balance.balance().scale()).isEqualTo(4);
        assertThat(balance.entryCount()).isEqualTo(7L);
    }

    @Test
    void defaultsNullCreditTotalToZeroWhenDebitExists() {
        GetAccountBalanceService service = new GetAccountBalanceService(
            entryRepository,
            auditEventPublisher,
            new LedgerMetrics(new SimpleMeterRegistry()),
            CLOCK);
        when(entryRepository.summarizePostedBalance(new AccountId("cash"), "USD"))
            .thenReturn(new BalanceSnapshot(new BigDecimal("12.3400"), null, 5L));

        AccountBalance balance = service.getBalance(new AccountId("cash"), "USD");

        assertThat(balance.balance()).isEqualByComparingTo("12.3400");
        assertThat(balance.balance().scale()).isEqualTo(4);
        assertThat(balance.entryCount()).isEqualTo(5L);
    }
}
