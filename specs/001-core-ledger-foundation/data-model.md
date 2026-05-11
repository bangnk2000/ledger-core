# Data Model: Core Ledger Foundation

## Ledger Transaction

Represents one accepted or rejected posting request outcome at the ledger boundary.

### Fields

- `id`: System-generated transaction identifier.
- `request_id`: Stable request identifier supplied by caller for idempotency.
- `requester_scope`: Caller or source scope used with `request_id` for uniqueness.
- `request_hash`: Canonical hash of the posting request content for conflict detection.
- `status`: `POSTED`, `REJECTED`, or `FAILED`.
- `business_reference`: Optional external business event reference.
- `description`: Optional human-readable posting purpose.
- `metadata`: Bounded JSON object for business context.
- `correlation_id`: Request or workflow correlation identifier.
- `causation_id`: Optional upstream event or command identifier.
- `actor_id`: Actor or system principal that initiated the request.
- `actor_type`: `USER`, `SYSTEM`, or `SERVICE`.
- `submitted_at`: Caller submission timestamp when provided.
- `posted_at`: Server timestamp for accepted posting.
- `rejection_code`: Stable code for rejected requests.
- `rejection_reason`: Sanitized rejection reason.
- `created_at`: Server creation timestamp.

### Relationships

- Has many `Ledger Entry` records.
- Has one durable idempotency identity through `requester_scope` + `request_id`.
- Produces audit and operational events.

### Validation Rules

- `requester_scope` + `request_id` must be unique for ledger mutation requests.
- Accepted transactions must contain at least two entries.
- Accepted transactions must contain at least one debit and at least one credit.
- Accepted transactions must have exact debit total equal to exact credit total.
- Metadata and trace fields must be size-bounded and sanitized.

### State Transitions

- `RECEIVED -> POSTED` when validation and persistence succeed.
- `RECEIVED -> REJECTED` when input or domain rules fail.
- `RECEIVED -> FAILED` when an unexpected system failure prevents a committed posting outcome.
- Posted transactions are immutable; corrections create a new transaction.

## Ledger Entry

Immutable debit or credit record posted to an account as part of a transaction.

### Fields

- `id`: System-generated entry identifier.
- `transaction_id`: Parent ledger transaction identifier.
- `line_id`: Caller-provided line identifier unique within the transaction.
- `account_id`: Stable account reference.
- `direction`: `DEBIT` or `CREDIT`.
- `amount`: Positive fixed-precision numeric amount.
- `currency`: Currency code; fixed to the feature currency for this slice.
- `entry_metadata`: Bounded JSON object for line-level context.
- `posted_at`: Server timestamp inherited from transaction posting.
- `created_at`: Server creation timestamp.

### Relationships

- Belongs to one `Ledger Transaction`.
- Contributes to derived `Account Balance`.

### Validation Rules

- Entry amount must be positive and finite.
- Direction must be present.
- Account reference must be present.
- `line_id` must be unique within the posting request.
- Financial fields, account reference, transaction reference, line reference, and trace details must not be changed after posting.

### State Transitions

- Entries are created only as part of an atomic transaction posting.
- Entries have no mutable lifecycle after posting.
- Corrections are represented by additional reversing or adjusting entries in a new transaction.

## Posting Request

Command submitted by a caller to create a ledger transaction.

### Fields

- `request_id`: Stable caller-provided idempotency key.
- `requester_scope`: Caller or source system scope.
- `entries`: Two or more requested ledger lines.
- `metadata`: Business metadata.
- `correlation_id`: Request/workflow correlation identifier.
- `causation_id`: Optional upstream event/command identifier.
- `actor`: Actor details.
- `submitted_at`: Optional caller timestamp.

### Validation Rules

- Must include stable request identity.
- Must include at least one debit and at least one credit.
- Must reject zero, negative, missing, or non-finite amounts.
- Must reject duplicate line identifiers.
- Must reject unbalanced debit and credit totals.
- Reuse of `request_id` with identical content returns the stored outcome.
- Reuse of `request_id` with conflicting content returns a conflict outcome and creates no ledger entries.

## Idempotency Record

Durable duplicate detection record for mutating ledger requests.

### Fields

- `requester_scope`: Caller or source scope.
- `request_id`: Stable caller-provided request identifier.
- `request_hash`: Canonical hash of request content.
- `outcome`: `ACCEPTED`, `REJECTED`, `CONFLICT`, or `FAILED`.
- `transaction_id`: Accepted or rejected transaction reference when stored.
- `response_code`: Stable API outcome code.
- `created_at`: First-seen timestamp.
- `last_seen_at`: Most recent duplicate attempt timestamp.

### Validation Rules

- Unique key on `requester_scope` + `request_id`.
- Existing matching hash returns the original durable outcome.
- Existing mismatched hash returns conflict and does not modify ledger history.

## Account Balance

Calculated view of an account position from posted ledger entries.

### Fields

- `account_id`: Account reference.
- `currency`: Currency code.
- `balance`: Net derived balance.
- `calculated_at`: Timestamp of calculation.
- `entry_count`: Number of posted entries included.

### Calculation Rule

For this foundation, calculate from committed posted entries only. Debit and credit sign interpretation must be explicit in the application query and documented in tests. Pending, rejected, failed, or conflicting requests do not affect balances.

## Audit Trace

Retained evidence connecting a transaction and entries to the initiating actor, request, and workflow.

### Fields

- `transaction_id`: Related ledger transaction.
- `request_id`: Stable request identifier.
- `requester_scope`: Caller/source scope.
- `correlation_id`: Workflow correlation identifier.
- `causation_id`: Optional upstream event/command identifier.
- `actor_id`: Initiating actor.
- `actor_type`: Initiating actor category.
- `event_type`: `POSTING_ACCEPTED`, `POSTING_REJECTED`, `DUPLICATE_REQUEST`, `CONFLICTING_REQUEST`, `POSTING_FAILED`, or `BALANCE_CALCULATED`.
- `event_time`: Server timestamp.
- `safe_details`: Sanitized structured details.

### Validation Rules

- Audit details must not expose secrets, internal SQL, stack traces, or sensitive financial data beyond authorized access.
- Accepted postings must be traceable from every entry to transaction, request, actor, and correlation details.
