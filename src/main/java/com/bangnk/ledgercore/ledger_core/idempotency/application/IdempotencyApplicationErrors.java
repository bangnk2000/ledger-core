package com.bangnk.ledgercore.ledger_core.idempotency.application;

public final class IdempotencyApplicationErrors {
    private IdempotencyApplicationErrors() {}

    public static class IdempotencyException extends RuntimeException {
        public IdempotencyException(String message) {
            super(message);
        }
    }

    public static class ClaimNotFoundException extends IdempotencyException {
        public ClaimNotFoundException(String message) {
            super(message);
        }
    }

    public static class ClaimOwnerMismatchException extends IdempotencyException {
        public ClaimOwnerMismatchException(String message) {
            super(message);
        }
    }

    public static class InvalidRecordStateException extends IdempotencyException {
        public InvalidRecordStateException(String message) {
            super(message);
        }
    }

    public static class DuplicateFinalizationException extends IdempotencyException {
        public DuplicateFinalizationException(String message) {
            super(message);
        }
    }
}
