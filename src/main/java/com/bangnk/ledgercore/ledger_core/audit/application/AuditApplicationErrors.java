package com.bangnk.ledgercore.ledger_core.audit.application;

public final class AuditApplicationErrors {
    private AuditApplicationErrors() {}

    public static class AuditException extends RuntimeException {
        public AuditException(String message) {
            super(message);
        }
    }

    public static class AuditCaptureException extends AuditException {
        public AuditCaptureException(String message) {
            super(message);
        }
    }

    public static class AuditAccessDeniedException extends AuditException {
        public AuditAccessDeniedException(String message) {
            super(message);
        }
    }

    public static class AuditIntegrityFailureException extends AuditException {
        public AuditIntegrityFailureException(String message) {
            super(message);
        }
    }

    public static class AuditEventNotFoundException extends AuditException {
        public AuditEventNotFoundException(String message) {
            super(message);
        }
    }
}
