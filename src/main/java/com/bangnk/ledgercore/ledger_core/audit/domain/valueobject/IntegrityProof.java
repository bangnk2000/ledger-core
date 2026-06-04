package com.bangnk.ledgercore.ledger_core.audit.domain.valueobject;

import com.bangnk.ledgercore.ledger_core.audit.domain.model.AuditEnums.IntegrityVerificationStatus;
import java.time.Instant;
import java.util.Objects;

public record IntegrityProof(
    String proofVersion,
    String contentDigest,
    String chainReference,
    Instant verifiedAt,
    IntegrityVerificationStatus verificationStatus
) {
    public IntegrityProof {
        proofVersion = normalizeRequired(proofVersion, "proofVersion", 32);
        contentDigest = normalizeRequired(contentDigest, "contentDigest", 256);
        chainReference = normalizeOptional(chainReference, "chainReference", 256);
        Objects.requireNonNull(verificationStatus, "verificationStatus must not be null");
    }

    public IntegrityProof verifiedAt(Instant when) {
        return new IntegrityProof(proofVersion, contentDigest, chainReference, when, IntegrityVerificationStatus.VERIFIED);
    }

    public IntegrityProof failedAt(Instant when) {
        return new IntegrityProof(proofVersion, contentDigest, chainReference, when, IntegrityVerificationStatus.FAILED);
    }

    private static String normalizeRequired(String value, String field, int maxLength) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be at most " + maxLength + " characters");
        }
        return value;
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
