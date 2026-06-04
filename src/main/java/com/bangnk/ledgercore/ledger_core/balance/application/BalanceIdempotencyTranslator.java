package com.bangnk.ledgercore.ledger_core.balance.application;

import com.bangnk.ledgercore.ledger_core.balance.application.command.BalanceMutationRequest;
import com.bangnk.ledgercore.ledger_core.idempotency.application.CanonicalRequestFingerprintFactory;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyKey;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyScope;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.RequestFingerprint;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BalanceIdempotencyTranslator {

    private static final String CANONICALIZATION_PROFILE = "balance-mutation-v1";

    private final CanonicalRequestFingerprintFactory fingerprintFactory;

    public BalanceIdempotencyTranslator(CanonicalRequestFingerprintFactory fingerprintFactory) {
        this.fingerprintFactory = fingerprintFactory;
    }

    public IdempotencyScope toScope(BalanceMutationRequest request) {
        return new IdempotencyScope(
            "balance-requester-scope",
            request.requestIdentity().requesterScope(),
            request.mutationType().name(),
            "DEFAULT"
        );
    }

    public IdempotencyKey toKey(BalanceMutationRequest request) {
        return new IdempotencyKey(request.requestIdentity().requestId());
    }

    public RequestFingerprint toFingerprint(BalanceMutationRequest request) {
        return fingerprintFactory.create(
            CANONICALIZATION_PROFILE,
            Map.of(
                "mutationType", request.mutationType().name(),
                "accountIds", accountIds(request),
                "currency", request.currency().value(),
                "amount", request.amount().value().toPlainString(),
                "direction", request.direction().name(),
                "businessReference", request.businessReference(),
                "expiresAt", request.expiresAt() != null ? request.expiresAt().toString() : null
            )
        );
    }

    private List<String> accountIds(BalanceMutationRequest request) {
        return request.accountIds().stream().map(accountId -> accountId.value()).toList();
    }
}
