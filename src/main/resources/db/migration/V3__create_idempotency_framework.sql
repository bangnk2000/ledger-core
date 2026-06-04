CREATE TABLE idempotency_records (
    id UUID PRIMARY KEY,
    scope_type VARCHAR(128) NOT NULL,
    scope_value VARCHAR(128) NOT NULL,
    operation_kind VARCHAR(128) NOT NULL,
    policy_profile VARCHAR(128) NOT NULL,
    key_value VARCHAR(256) NOT NULL,
    issued_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    fingerprint_value VARCHAR(256) NOT NULL,
    fingerprint_version VARCHAR(64) NOT NULL,
    material_fields_summary VARCHAR(512),
    canonicalization_profile VARCHAR(128),
    state VARCHAR(64) NOT NULL,
    claim_owner VARCHAR(128),
    claim_acquired_at TIMESTAMPTZ,
    last_transition_at TIMESTAMPTZ NOT NULL,
    first_seen_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    replay_window_expires_at TIMESTAMPTZ,
    tombstone_expires_at TIMESTAMPTZ,
    retention_status VARCHAR(64) NOT NULL,
    business_reference VARCHAR(128),
    correlation_id VARCHAR(128),
    causation_id VARCHAR(128),
    attempt_count INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_idempotency_records_scope_key UNIQUE (scope_type, scope_value, operation_kind, key_value)
);

CREATE INDEX idx_idempotency_records_retention ON idempotency_records(retention_status, tombstone_expires_at);
CREATE INDEX idx_idempotency_records_lookup ON idempotency_records(scope_type, scope_value, operation_kind, key_value);

CREATE TABLE idempotency_replay_outcomes (
    record_id UUID PRIMARY KEY REFERENCES idempotency_records(id) ON DELETE CASCADE,
    outcome_type VARCHAR(64) NOT NULL,
    response_code VARCHAR(128),
    response_payload TEXT,
    http_status_hint INT,
    business_result_reference VARCHAR(128),
    finalized_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE idempotency_lifecycle_events (
    id UUID PRIMARY KEY,
    record_id UUID NOT NULL REFERENCES idempotency_records(id) ON DELETE CASCADE,
    event_type VARCHAR(64) NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_type VARCHAR(32) NOT NULL,
    actor_id VARCHAR(128) NOT NULL,
    details JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_idempotency_lifecycle_events_record ON idempotency_lifecycle_events(record_id);
