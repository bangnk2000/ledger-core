# Research: Audit Trail Framework

## Decision: Introduce `audit` as a shared sibling bounded context

**Rationale**: The repository already has ledger-local structured audit logging,
ledger `AuditTrace`, balance `ActorContext`, and the new shared `idempotency`
module. A dedicated sibling `audit` module under `ledger_core` keeps the audit
capability reusable across bounded contexts without forcing future consumers to
depend on ledger-specific value objects or ad hoc log formatting. This aligns
with the modular monolith ADR and preserves a clear future extraction path.

**Alternatives considered**: Embedding the framework inside `ledger` was
rejected because balance and future modules would then depend on accounting
internals. Leaving audit behavior as module-local logging was rejected because
it cannot provide durable, centralized, investigation-grade evidence. Building
an external service first was rejected because it would add distributed
transaction and delivery complexity before the internal contracts stabilize.

## Decision: Treat durable audit capture as mandatory, and downstream publication as non-blocking

**Rationale**: The specification requires that audit data never cause business
transaction failure solely because downstream publication is unavailable. The
safest model is to make durable capture part of the protected write path while
treating downstream publication, indexing, or export as a backlog-driven
secondary flow. This preserves evidence even during publication outages and
matches the existing repo preference for explicit transaction boundaries.

**Alternatives considered**: Fire-and-forget logging only was rejected because
it is not sufficient for regulatory or investigation use. Synchronous
publication inside the main transaction was rejected because a transient
consumer outage would violate the requirement to avoid impacting business
success. Full eventual-only capture after business commit was rejected because
it risks losing the authoritative audit event during crashes.

## Decision: Standardize actor and trace context across modules with explicit “unavailable” semantics

**Rationale**: The current codebase uses `AuditTrace` in ledger and
`ActorContext` in balance, but they do not yet provide one shared audit
contract. The framework should normalize actor type, actor identifier, origin,
correlation identifier, request identifier, and causation identifier into one
shared audit contract. When a value is missing, the framework must preserve
that absence explicitly rather than fabricating a placeholder or rejecting the
event outright.

**Alternatives considered**: Requiring every event to have a human actor was
rejected because scheduled jobs and service actors are first-class
requirements. Treating missing values as null without explicit semantics was
rejected because investigators need to distinguish unknown, unavailable, and
not-applicable cases.

## Decision: Make ledger and idempotency linkages first-class optional references

**Rationale**: Clarification established that audit events must carry explicit
ledger transaction and idempotency references whenever those objects exist, and
mark them absent otherwise. For idempotency-protected actions, both the
business-facing idempotency key and the durable idempotency record identifier
should be recorded when available. This gives investigators a direct path from
caller-visible retries to stored duplicate-handling evidence and from business
events to immutable ledger postings.

**Alternatives considered**: Keeping ledger or idempotency linkage indirect via
business references only was rejected because investigation and replay analysis
would require cross-module guesswork. Requiring every event to carry both
references was rejected because many modules or event types will legitimately
lack one or both objects.

## Decision: Use append-only audit events plus tamper-evident integrity linkage

**Rationale**: The feature requires append-only and tamper-evident audit
records. The simplest design that satisfies both is an append-only event store
where each audit record carries immutable content plus integrity-link metadata
that can detect out-of-band mutation, deletion, or reordering. The first
implementation should stay storage-native to PostgreSQL and application-visible
without introducing external cryptographic infrastructure or event sourcing.

**Alternatives considered**: Mutable current-state audit rows were rejected
because they erase historical evidence. Full external WORM storage or managed
ledger services were rejected because they add operational complexity and new
dependencies too early. Generic application logs alone were rejected because
they do not provide durable investigation-grade queryability or linkage
guarantees.

## Decision: Preserve existing ledger structured audit logging during phased rollout

**Rationale**: `StructuredAuditEventPublisher` already emits structured ledger
audit logs, and ledger transactions already embed `AuditTrace`. Replacing those
paths in one step would widen the migration blast radius. The first rollout
should introduce the shared audit framework additively, onboard ledger and
balance one workflow at a time, and allow legacy structured logging to coexist
until characterization tests confirm parity and operators are comfortable with
the new evidence path.

**Alternatives considered**: Big-bang replacement of existing audit/logging
paths was rejected because it combines platform capability design with risky
behavioral migration. Leaving legacy and new paths permanently divergent was
rejected because it would fragment future investigation workflows.

## Decision: Provide authorized investigation queries through framework-owned read contracts

**Rationale**: The framework is not just a write-time capture concern; support,
security, and compliance need durable investigation access by business object,
event type, actor, correlation identifier, request identifier, ledger
transaction reference, and idempotency linkage. These read contracts must be
owned by the framework so future modules and services use one canonical
investigation model instead of inventing separate search endpoints or direct
database access patterns.

**Alternatives considered**: Leaving query behavior to each consumer module was
rejected because it would reintroduce inconsistent terminology and access
rules. Exposing raw SQL or JPA entities to operational tooling was rejected
because it violates hexagonal boundaries and weakens security review.

## Decision: Use staged retention with retained evidence after active query expiry

**Rationale**: Financial and regulatory audit requirements need long-lived
evidence, but not every audit event must remain fully queryable at the same
level forever. The framework should support active investigation retention,
restricted evidence retention, and final disposition according to policy, while
ensuring that retention transitions themselves are auditable and that enough
proof remains to demonstrate prior record existence and policy compliance.

**Alternatives considered**: Infinite full-fidelity retention was rejected
because it makes storage growth and privacy control unbounded. Immediate hard
deletion after a fixed period was rejected because it can destroy evidence
required for compliance reviews or late investigations.
