package com.bangnk.ledgercore.ledger_core.audit.domain.valueobject;

import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.ReferenceStatus;
import java.util.Objects;

public record LedgerTransactionReference(
    String transactionId,
    String postingType,
    ReferenceStatus referenceStatus
) {
    public LedgerTransactionReference {
        Objects.requireNonNull(referenceStatus, "referenceStatus must not be null");
        transactionId = normalizeOptional(transactionId, "transactionId", 128);
        postingType = normalizeOptional(postingType, "postingType", 64);
        if (referenceStatus == ReferenceStatus.PRESENT && transactionId == null) {
            throw new IllegalArgumentException("transactionId must be present when referenceStatus is PRESENT");
        }
    }

    private static String normalizeOptional(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be at most " + maxLength + " characters");
        }
        return value;
    }
}
