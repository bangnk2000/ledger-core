CREATE TABLE ledger_transactions (
    id UUID PRIMARY KEY,
    requester_scope VARCHAR(128) NOT NULL,
    request_id VARCHAR(128) NOT NULL,
    request_hash VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    business_reference VARCHAR(128),
    description VARCHAR(512),
    metadata JSONB,
    correlation_id VARCHAR(128),
    causation_id VARCHAR(128),
    actor_id VARCHAR(128) NOT NULL,
    actor_type VARCHAR(32) NOT NULL,
    submitted_at TIMESTAMPTZ,
    posted_at TIMESTAMPTZ,
    rejection_code VARCHAR(128),
    rejection_reason VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ledger_transactions_request UNIQUE (requester_scope, request_id)
);

CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES ledger_transactions(id),
    line_id VARCHAR(128) NOT NULL,
    account_id VARCHAR(128) NOT NULL,
    direction VARCHAR(16) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL,
    entry_metadata JSONB,
    posted_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ledger_entries_transaction_line UNIQUE (transaction_id, line_id)
);

CREATE INDEX idx_ledger_entries_account_currency_posted
    ON ledger_entries (account_id, currency, posted_at);

CREATE TABLE ledger_idempotency_records (
    requester_scope VARCHAR(128) NOT NULL,
    request_id VARCHAR(128) NOT NULL,
    request_hash VARCHAR(128) NOT NULL,
    outcome VARCHAR(32) NOT NULL,
    transaction_id UUID,
    response_code VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (requester_scope, request_id)
);

CREATE OR REPLACE FUNCTION prevent_ledger_entry_mutation()
RETURNS TRIGGER AS $$
BEGIN
    IF (OLD.amount IS DISTINCT FROM NEW.amount)
        OR (OLD.direction IS DISTINCT FROM NEW.direction)
        OR (OLD.account_id IS DISTINCT FROM NEW.account_id)
        OR (OLD.transaction_id IS DISTINCT FROM NEW.transaction_id)
        OR (OLD.line_id IS DISTINCT FROM NEW.line_id)
        OR (OLD.currency IS DISTINCT FROM NEW.currency)
        OR (OLD.posted_at IS DISTINCT FROM NEW.posted_at) THEN
        RAISE EXCEPTION 'ledger entries are immutable';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER ledger_entries_immutable_before_update
    BEFORE UPDATE ON ledger_entries
    FOR EACH ROW
    EXECUTE FUNCTION prevent_ledger_entry_mutation();

CREATE OR REPLACE FUNCTION prevent_ledger_entry_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'ledger entries cannot be deleted';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER ledger_entries_immutable_before_delete
    BEFORE DELETE ON ledger_entries
    FOR EACH ROW
    EXECUTE FUNCTION prevent_ledger_entry_delete();
