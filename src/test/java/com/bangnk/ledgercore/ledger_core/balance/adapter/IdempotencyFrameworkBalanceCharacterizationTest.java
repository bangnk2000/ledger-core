package com.bangnk.ledgercore.ledger_core.balance.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bangnk.ledgercore.ledger_core.balance.application.BalanceIdempotencyTranslator;
import com.bangnk.ledgercore.ledger_core.balance.application.command.BalanceMutationRequest;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceActorType;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.ActorContext;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.MoneyAmount;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import com.bangnk.ledgercore.ledger_core.idempotency.application.CanonicalRequestFingerprintFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class IdempotencyFrameworkBalanceCharacterizationTest {

    private final BalanceIdempotencyTranslator translator =
        new BalanceIdempotencyTranslator(new CanonicalRequestFingerprintFactory(new ObjectMapper()));

    @Test
    void excludesIdentityAndActorTransportFieldsFromCanonicalFingerprint() {
        BalanceMutationRequest first = request("req-1", "corr-1");
        BalanceMutationRequest retry = request("req-2", "corr-2");

        assertEquals(
            translator.toFingerprint(first).fingerprintValue(),
            translator.toFingerprint(retry).fingerprintValue()
        );
    }

    private static BalanceMutationRequest request(String requestId, String correlationId) {
        return new BalanceMutationRequest(
            new RequestIdentity("tenant-a", requestId),
            BalanceMutationType.RESERVE,
            List.of(new AccountId("cash-1")),
            new CurrencyCode("USD"),
            new MoneyAmount(new BigDecimal("100.0000")),
            BalanceDirection.DEBIT,
            new ActorContext("svc-1", BalanceActorType.SERVICE, correlationId, null),
            "reservation-1",
            Instant.parse("2026-06-05T00:00:00Z")
        );
    }
}
