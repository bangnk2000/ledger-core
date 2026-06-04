CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(64) NOT NULL,
    schema_version VARCHAR(32) NOT NULL,
    module_name VARCHAR(128) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    captured_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subject_type VARCHAR(128) NOT NULL,
    subject_id VARCHAR(128) NOT NULL,
    business_reference VARCHAR(128),
    state_from VARCHAR(128),
    state_to VARCHAR(128),
    safe_details JSONB,
    actor_type VARCHAR(32) NOT NULL,
    actor_id VARCHAR(128),
    actor_origin VARCHAR(128),
    authority_context VARCHAR(128),
    actor_presence_status VARCHAR(32) NOT NULL,
    correlation_id VARCHAR(128),
    request_id VARCHAR(128),
    causation_id VARCHAR(128),
    trace_presence_status VARCHAR(32) NOT NULL,
    ledger_transaction_id VARCHAR(128),
    ledger_posting_type VARCHAR(64),
    ledger_reference_status VARCHAR(32) NOT NULL,
    idempotency_key VARCHAR(256),
    idempotency_record_id VARCHAR(128),
    idempotency_scope_type VARCHAR(128),
    idempotency_scope_value VARCHAR(128),
    idempotency_reference_status VARCHAR(32) NOT NULL,
    retention_profile VARCHAR(128) NOT NULL,
    active_retention_until TIMESTAMPTZ,
    restricted_retention_until TIMESTAMPTZ,
    final_disposition_rule VARCHAR(64),
    regulatory_classification VARCHAR(64),
    integrity_proof_version VARCHAR(32) NOT NULL,
    integrity_content_digest VARCHAR(256) NOT NULL,
    integrity_chain_reference VARCHAR(256),
    integrity_verified_at TIMESTAMPTZ,
    integrity_verification_status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_events_subject ON audit_events(subject_type, subject_id, occurred_at);
CREATE INDEX idx_audit_events_correlation ON audit_events(correlation_id, occurred_at);
CREATE INDEX idx_audit_events_request ON audit_events(request_id, occurred_at);
CREATE INDEX idx_audit_events_ledger ON audit_events(ledger_transaction_id, occurred_at);
CREATE INDEX idx_audit_events_idempotency_key ON audit_events(idempotency_key, occurred_at);
CREATE INDEX idx_audit_events_idempotency_record ON audit_events(idempotency_record_id, occurred_at);
CREATE INDEX idx_audit_events_module_type ON audit_events(module_name, event_type, occurred_at);
CREATE INDEX idx_audit_events_retention ON audit_events(restricted_retention_until, active_retention_until);

CREATE TABLE audit_publication_attempts (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES audit_events(id) ON DELETE CASCADE,
    destination_type VARCHAR(64) NOT NULL,
    attempted_at TIMESTAMPTZ NOT NULL,
    result VARCHAR(32) NOT NULL,
    failure_reason VARCHAR(256),
    next_retry_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_publication_attempts_event ON audit_publication_attempts(event_id, attempted_at);
CREATE INDEX idx_audit_publication_attempts_retry ON audit_publication_attempts(result, next_retry_at);
