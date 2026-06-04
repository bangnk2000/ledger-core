package com.bangnk.ledgercore.ledger_core.idempotency.application;

import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.RequestFingerprint;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Component;

@Component
public class CanonicalRequestFingerprintFactory {

    private static final String DEFAULT_FINGERPRINT_VERSION = "v1";

    private final ObjectMapper objectMapper;

    public CanonicalRequestFingerprintFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy()
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public RequestFingerprint create(String canonicalizationProfile, Map<String, ?> materialFields) {
        Map<String, Object> normalized = new TreeMap<>();
        normalized.putAll(materialFields);
        String canonicalJson = toCanonicalJson(normalized);
        return new RequestFingerprint(
            sha256(canonicalJson),
            DEFAULT_FINGERPRINT_VERSION,
            summarize(normalized),
            canonicalizationProfile
        );
    }

    private String toCanonicalJson(Map<String, ?> normalizedFields) {
        try {
            return objectMapper.writeValueAsString(normalizedFields);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to canonicalize request fingerprint", ex);
        }
    }

    private String sha256(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to hash canonical request", ex);
        }
    }

    private String summarize(Map<String, ?> normalizedFields) {
        return String.join(",", normalizedFields.keySet());
    }
}
