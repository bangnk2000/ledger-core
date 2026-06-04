package com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject;

import java.util.Objects;

public record RequestFingerprint(
    String fingerprintValue,
    String fingerprintVersion,
    String materialFieldsSummary,
    String canonicalizationProfile
) {
    public RequestFingerprint {
        Objects.requireNonNull(fingerprintValue, "fingerprintValue must not be null");
        Objects.requireNonNull(fingerprintVersion, "fingerprintVersion must not be null");
        if (fingerprintValue.isBlank()) throw new IllegalArgumentException("fingerprintValue must not be blank");
        if (fingerprintVersion.isBlank()) throw new IllegalArgumentException("fingerprintVersion must not be blank");
    }

    public boolean matches(RequestFingerprint other) {
        return other != null
            && fingerprintValue.equals(other.fingerprintValue)
            && fingerprintVersion.equals(other.fingerprintVersion)
            && Objects.equals(canonicalizationProfile, other.canonicalizationProfile);
    }

    public boolean conflictsWith(RequestFingerprint other) {
        return !matches(other);
    }
}
