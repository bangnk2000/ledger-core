package com.bangnk.ledgercore.ledger_core.balance.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ReferenceStatus;
import com.bangnk.ledgercore.ledger_core.balance.application.BalanceAuditTranslator;
import com.bangnk.ledgercore.ledger_core.balance.application.command.BalanceMutationRequest;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceActorType;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuditFrameworkBalanceCharacterizationTest {

    private final BalanceAuditTranslator translator = new BalanceAuditTranslator();

    @Test
    void mapsBalanceReservationIntoSharedAuditCaptureCommand() {
        BalanceMutationRequest request = request();

        var auditCommand = translator.toCaptureCommand(request, "RESERVE_ACCEPTED", "ledger-txn-1", "record-1", "idem-1");

        assertEquals("balance", auditCommand.moduleName());
        assertEquals("balance-mutation", auditCommand.subjectType());
        assertEquals("reservation-1", auditCommand.subjectId());
        assertEquals(ActorType.SERVICE, auditCommand.actorIdentity().actorType());
        assertEquals("ledger-txn-1", auditCommand.ledgerTransactionReference().transactionId());
        assertEquals(ReferenceStatus.PRESENT, auditCommand.idempotencyReference().referenceStatus());
    }

    private static BalanceMutationRequest request() {
        return new BalanceMutationRequest(
            new RequestIdentity("tenant-a", "req-1"),
            BalanceMutationType.RESERVE,
            List.of(new AccountId("cash-1")),
            new CurrencyCode("USD"),
            new MoneyAmount(new BigDecimal("100.0000")),
            BalanceDirection.DEBIT,
            new ActorContext("svc-1", BalanceActorType.SERVICE, "corr-1", null),
            "reservation-1",
            Instant.parse("2026-06-05T00:00:00Z")
        );
    }
}
