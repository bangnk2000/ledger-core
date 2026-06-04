package com.bangnk.ledgercore.ledger_core.ledger.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ReferenceStatus;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.LedgerAuditTranslator;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionCommand;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction.LedgerEntryDraft;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LineId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.Money;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuditFrameworkLedgerCharacterizationTest {

    private final LedgerAuditTranslator translator = new LedgerAuditTranslator();

    @Test
    void mapsLedgerPostingIntoSharedAuditCaptureCommand() {
        PostLedgerTransactionCommand command = command();

        var auditCommand = translator.toCaptureCommand(command, "POSTING_ACCEPTED", "txn-1", "record-1", "idem-1");

        assertEquals("ledger", auditCommand.moduleName());
        assertEquals("ledger-transaction", auditCommand.subjectType());
        assertEquals("txn-1", auditCommand.subjectId());
        assertEquals(ActorType.SYSTEM, auditCommand.actorIdentity().actorType());
        assertEquals(ReferenceStatus.PRESENT, auditCommand.ledgerTransactionReference().referenceStatus());
        assertEquals("idem-1", auditCommand.idempotencyReference().idempotencyKey());
    }

    private static PostLedgerTransactionCommand command() {
        RequestIdentity requestIdentity = new RequestIdentity("tenant-a", "req-1");
        return new PostLedgerTransactionCommand(
            requestIdentity,
            new AuditTrace(
                requestIdentity,
                "corr-1",
                null,
                new AuditTrace.Actor("actor-1", com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType.SYSTEM),
                Instant.parse("2026-06-04T00:00:00Z")
            ),
            "business-1",
            "post",
            Map.of("channel", "api"),
            List.of(
                new LedgerEntryDraft(new LineId("debit-1"), new AccountId("cash"), Direction.DEBIT, new Money(new BigDecimal("100.0000"), "USD"), null),
                new LedgerEntryDraft(new LineId("credit-1"), new AccountId("revenue"), Direction.CREDIT, new Money(new BigDecimal("100.0000"), "USD"), null)
            )
        );
    }
}
