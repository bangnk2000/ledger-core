package com.bangnk.ledgercore.ledger_core.ledger.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bangnk.ledgercore.ledger_core.idempotency.application.CanonicalRequestFingerprintFactory;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.LedgerIdempotencyTranslator;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionCommand;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction.LedgerEntryDraft;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LineId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.Money;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IdempotencyFrameworkLedgerCharacterizationTest {

    private final LedgerIdempotencyTranslator translator =
        new LedgerIdempotencyTranslator(new CanonicalRequestFingerprintFactory(new ObjectMapper()));

    @Test
    void excludesTransportAuditMetadataFromCanonicalFingerprint() {
        PostLedgerTransactionCommand first = command("req-1", "corr-1");
        PostLedgerTransactionCommand retry = command("req-2", "corr-2");

        assertEquals(
            translator.toFingerprint(first).fingerprintValue(),
            translator.toFingerprint(retry).fingerprintValue()
        );
    }

    private static PostLedgerTransactionCommand command(String requestId, String correlationId) {
        RequestIdentity requestIdentity = new RequestIdentity("tenant-a", requestId);
        return new PostLedgerTransactionCommand(
            requestIdentity,
            new AuditTrace(
                requestIdentity,
                correlationId,
                null,
                new AuditTrace.Actor("actor-1", ActorType.SYSTEM),
                Instant.parse("2026-06-04T00:00:00Z")
            ),
            "business-1",
            "post",
            Map.of("traceOnly", correlationId),
            List.of(
                new LedgerEntryDraft(new LineId("debit-1"), new AccountId("cash"), Direction.DEBIT, new Money(new BigDecimal("100.0000"), "USD"), null),
                new LedgerEntryDraft(new LineId("credit-1"), new AccountId("revenue"), Direction.CREDIT, new Money(new BigDecimal("100.0000"), "USD"), null)
            )
        );
    }
}
