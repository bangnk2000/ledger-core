package com.bangnk.ledgercore.ledger_core.audit.domain.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record InvestigationView(
    String viewId,
    Map<String, String> matchCriteria,
    List<AuditEvent> orderedEvents,
    List<String> linkedSubjects,
    String accessScope
) {
    public InvestigationView {
        viewId = normalizeRequired(viewId, "viewId", 128);
        matchCriteria = matchCriteria == null ? Map.of() : Map.copyOf(matchCriteria);
        orderedEvents = orderedEvents == null ? List.of() : List.copyOf(orderedEvents);
        linkedSubjects = linkedSubjects == null ? List.of() : List.copyOf(linkedSubjects);
        accessScope = normalizeRequired(accessScope, "accessScope", 128);
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
}
