CREATE TABLE balance_state (
    account_id VARCHAR(128) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    ledger_balance NUMERIC(19, 4) NOT NULL DEFAULT 0 CHECK (ledger_balance >= 0),
    locked_amount NUMERIC(19, 4) NOT NULL DEFAULT 0 CHECK (locked_amount >= 0),
    pending_debit_amount NUMERIC(19, 4) NOT NULL DEFAULT 0 CHECK (pending_debit_amount >= 0),
    pending_credit_amount NUMERIC(19, 4) NOT NULL DEFAULT 0 CHECK (pending_credit_amount >= 0),
    available_balance NUMERIC(19, 4) GENERATED ALWAYS AS (
        ledger_balance - locked_amount - pending_debit_amount + pending_credit_amount
    ) STORED,
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    ledger_as_of_sequence BIGINT NOT NULL DEFAULT 0 CHECK (ledger_as_of_sequence >= 0),
    reservation_as_of_sequence BIGINT NOT NULL DEFAULT 0 CHECK (reservation_as_of_sequence >= 0),
    reconciliation_status VARCHAR(32) NOT NULL DEFAULT 'HEALTHY',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, currency)
);

CREATE INDEX idx_balance_state_reconciliation_status
    ON balance_state (reconciliation_status);

CREATE TABLE funds_reservations (
    reservation_id UUID PRIMARY KEY,
    requester_scope VARCHAR(128) NOT NULL,
    request_id VARCHAR(128) NOT NULL,
    account_id VARCHAR(128) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    direction VARCHAR(16) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    business_reference VARCHAR(128),
    status VARCHAR(32) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    ledger_transaction_id VARCHAR(128),
    confirmation_reference VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_funds_reservations_balance_state
        FOREIGN KEY (account_id, currency) REFERENCES balance_state (account_id, currency),
    CONSTRAINT uk_funds_reservations_request UNIQUE (requester_scope, request_id)
);

CREATE INDEX idx_funds_reservations_account_status
    ON funds_reservations (account_id, currency, status);

CREATE INDEX idx_funds_reservations_expires_at
    ON funds_reservations (expires_at)
    WHERE status = 'ACTIVE';

CREATE TABLE balance_snapshots (
    snapshot_id UUID PRIMARY KEY,
    account_id VARCHAR(128) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    ledger_balance NUMERIC(19, 4) NOT NULL,
    locked_amount NUMERIC(19, 4) NOT NULL,
    pending_debit_amount NUMERIC(19, 4) NOT NULL,
    pending_credit_amount NUMERIC(19, 4) NOT NULL,
    available_balance NUMERIC(19, 4) NOT NULL,
    snapshot_version BIGINT NOT NULL CHECK (snapshot_version >= 0),
    as_of_sequence BIGINT NOT NULL CHECK (as_of_sequence >= 0),
    as_of_time TIMESTAMPTZ NOT NULL,
    consistency_mode VARCHAR(32) NOT NULL,
    reconciliation_status VARCHAR(32) NOT NULL DEFAULT 'HEALTHY',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_balance_snapshots_account_version
    ON balance_snapshots (account_id, currency, snapshot_version DESC);

CREATE TABLE balance_rebuild_jobs (
    job_id UUID PRIMARY KEY,
    scope TEXT NOT NULL,
    replay_contract_version VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    checkpoint_token TEXT,
    processed_record_count BIGINT NOT NULL DEFAULT 0 CHECK (processed_record_count >= 0),
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    requested_by VARCHAR(128) NOT NULL,
    failure_reason VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_balance_rebuild_jobs_status
    ON balance_rebuild_jobs (status, created_at);

CREATE TABLE balance_rebuild_checkpoints (
    checkpoint_id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES balance_rebuild_jobs (job_id),
    account_id VARCHAR(128),
    ledger_sequence BIGINT NOT NULL DEFAULT 0 CHECK (ledger_sequence >= 0),
    reservation_sequence BIGINT NOT NULL DEFAULT 0 CHECK (reservation_sequence >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_balance_rebuild_checkpoints_job_created
    ON balance_rebuild_checkpoints (job_id, created_at DESC);

CREATE TABLE balance_reconciliation_records (
    reconciliation_id UUID PRIMARY KEY,
    account_scope TEXT NOT NULL,
    expected_balance JSONB NOT NULL,
    actual_balance JSONB NOT NULL,
    difference_summary JSONB NOT NULL,
    severity VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    investigation_reference VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_balance_reconciliation_records_status
    ON balance_reconciliation_records (status, severity, created_at);

CREATE TABLE balance_idempotency_records (
    requester_scope VARCHAR(128) NOT NULL,
    request_id VARCHAR(128) NOT NULL,
    request_hash VARCHAR(128) NOT NULL,
    mutation_type VARCHAR(32) NOT NULL,
    outcome VARCHAR(32) NOT NULL,
    response_code VARCHAR(128) NOT NULL,
    response_payload JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (requester_scope, request_id)
);
