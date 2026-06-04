# Research: Idempotency Framework

## Decision: Introduce `idempotency` as a shared sibling bounded context

**Rationale**: The repository already contains ledger-specific and
balance-specific idempotency logic. A dedicated sibling module under
`ledger_core` makes the capability reusable for future modules without forcing
them to depend on ledger or balance internals. This aligns with modular
monolith boundaries and keeps dependency direction explicit.

**Alternatives considered**: Embedding the framework inside `ledger` was
rejected because balance and future modules would then depend on accounting
internals. A shared utility package was rejected because idempotency carries
domain rules, persistence contracts, and lifecycle behavior that deserve an
explicit bounded context rather than static helpers.

## Decision: Use scope-aware key identity plus canonical business-intent fingerprinting

**Rationale**: A raw idempotency key is not sufficient in a multi-tenant,
multi-workflow platform. The framework should treat uniqueness as
`scope + key + operation kind`, while using a request fingerprint to determine
whether a retry is semantically identical or a conflicting reuse. This keeps
duplicate detection reusable for APIs, jobs, webhooks, and future ledger flows.
The fingerprint should be derived from a canonical business-intent object using
normalized material fields only, excluding transport metadata such as headers,
request timestamps, trace IDs, or protocol-only wrappers.

**Alternatives considered**: Global key uniqueness was rejected because it
causes unnecessary collisions between unrelated workflows. Key-only matching
was rejected because it cannot safely distinguish a real retry from conflicting
request reuse. Full raw-payload hashing was rejected because transport-specific
noise would create false conflicts across different delivery channels.

## Decision: Model duplicate handling as a durable processing state machine plus retention status

**Rationale**: The framework must support first execution, in-flight duplicate
requests, completed replay, rejected reuse, expiration, and uncertain
downstream outcomes. A durable state machine with explicit transitions is safer
than an implicit “exists or not” record because it preserves auditability and
supports recovery decisions after crashes or partial failures. The processing
lifecycle should remain `RECEIVED -> CLAIMED -> PROCESSING -> COMPLETED |
REJECTED | INDETERMINATE`, while expiration should be handled after terminal
states as replay/retention handling rather than as a peer processing state.

**Alternatives considered**: A simple inserted-then-replayed record with no
state progression was rejected because it cannot model long-running processing,
stuck requests, or indeterminate downstream outcomes. Consumer-local ad hoc
flags were rejected because they would reintroduce duplicated framework logic.
Treating `EXPIRED` as a primary processing state was rejected because it blurs
transactional flow with retention cleanup concerns.

## Decision: Acquire one active claim through atomic persistence semantics

**Rationale**: To remain safe under horizontally scaled application instances,
the framework needs a persistence contract that allows exactly one caller to
win the active claim for a given scope/key/fingerprint while all competing
duplicates observe a stable result. The initial PostgreSQL adapter should use
unique constraints and conditional state transitions, while the application
contract remains storage-agnostic.

**Implementation note**: The current adapter realizes this with an additive
unique constraint plus `INSERT ... ON CONFLICT DO NOTHING`, followed by a
re-read for competing callers to resolve `DUPLICATE_IN_PROGRESS`, `REPLAY`, or
`CONFLICT` deterministically.

**Alternatives considered**: In-memory locking was rejected because it fails
across instances and restarts. Queue-based serialization was rejected because
it adds infrastructure and latency beyond the current requirement.

## Decision: Use two-stage retention with replay window then tombstone window

**Rationale**: Financial and integration workflows need bounded storage without
making late retries unsafe. The safest default is a replayable retention window
followed by an expired tombstone window that still prevents accidental
re-execution but no longer replays the full original outcome indefinitely.
Cleanup can archive or prune only after the tombstone window ends.

**Alternatives considered**: Immediate hard deletion at expiration was rejected
because a late retry could be mistaken for a brand-new request. Permanent
indefinite retention was rejected because it makes storage growth and cleanup
unbounded.

**Implementation note**: Cleanup first tombstones records after replay expiry,
then purges only after tombstone expiry. Late retries during the tombstone
window return `EXPIRED_KEY` and remain blocked from re-execution.

## Decision: Preserve existing ledger and balance idempotency implementations during rollout

**Rationale**: The repository already has `ledger_idempotency_records` and
`balance_idempotency_records`. Replacing them in one step would create migration
risk and widen scope beyond the foundational framework. The first rollout
should add a generic `idempotency_records` store and consumer-facing contracts
that existing modules can adopt incrementally.

**Alternatives considered**: A big-bang migration of ledger and balance to the
new table was rejected because it combines platform capability design with
behavioral migration risk. Leaving the framework purely conceptual with no
persistence contract was rejected because the capability must be auditable and
safe under retries from day one.

## Decision: Standardize on internal application contracts before external delivery conventions

**Rationale**: The immediate value is a reusable internal capability for
services inside the modular monolith. Explicit claim/finalize/replay/cleanup
ports let future HTTP, webhook, job, and messaging adapters adopt the framework
without forcing one delivery protocol into the core design.

**Alternatives considered**: Defining the framework primarily as HTTP filters
or gateway behavior was rejected because API gateway details are out of scope
and asynchronous consumers need the same core semantics. Exposing no contracts
beyond repository interfaces was rejected because future consumers need a stable
application-facing integration model.
