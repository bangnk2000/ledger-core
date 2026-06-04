package com.bangnk.ledgercore.ledger_core.ledger.application.command;

import com.bangnk.ledgercore.ledger_core.idempotency.application.CanonicalRequestFingerprintFactory;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyKey;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyScope;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.RequestFingerprint;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction.LedgerEntryDraft;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LedgerIdempotencyTranslator {

    private static final String CANONICALIZATION_PROFILE = "ledger-posting-v1";

    private final CanonicalRequestFingerprintFactory fingerprintFactory;

    public LedgerIdempotencyTranslator(CanonicalRequestFingerprintFactory fingerprintFactory) {
        this.fingerprintFactory = fingerprintFactory;
    }

    public IdempotencyScope toScope(PostLedgerTransactionCommand command) {
        return new IdempotencyScope(
            "ledger-requester-scope",
            command.requestIdentity().requesterScope(),
            "post-ledger-transaction",
            "DEFAULT"
        );
    }

    public IdempotencyKey toKey(PostLedgerTransactionCommand command) {
        return new IdempotencyKey(command.requestIdentity().requestId());
    }

    public RequestFingerprint toFingerprint(PostLedgerTransactionCommand command) {
        return fingerprintFactory.create(
            CANONICALIZATION_PROFILE,
            Map.of(
                "businessReference", command.businessReference(),
                "description", command.description(),
                "entries", toEntryMaps(command.entries())
            )
        );
    }

    private List<Map<String, Object>> toEntryMaps(List<LedgerEntryDraft> entries) {
        return entries.stream()
            .map(entry -> Map.<String, Object>of(
                "lineId", entry.lineId().value(),
                "accountId", entry.accountId().value(),
                "direction", entry.direction().name(),
                "amount", entry.money().amount().toPlainString(),
                "currency", entry.money().currency()
            ))
            .toList();
    }
}
