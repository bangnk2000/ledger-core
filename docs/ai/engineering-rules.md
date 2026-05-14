# Ledger Core Engineering Rules

These rules are extracted from `AGENTS.md` and apply to implementation,
planning, and review work for ledger-core.

## Project Context

- Project name: ledger-core.
- Domain: financial ledger and accounting core.
- Primary stack: Java Spring Boot, PostgreSQL, Docker, Kubernetes, Terraform.
- Architecture: Hexagonal Architecture, Pragmatic DDD, modular monolith first.
- CQRS is allowed only when justified by a concrete modeling, scaling, or
  operational need.
- Zero-downtime deployment and rolling updates are mandatory.
- Public and internal APIs must remain backward compatible whenever possible.

## Mandatory Principles

- Prefer simplicity over premature abstraction.
- Financial correctness is more important than clever code.
- Ledger entries are immutable after posting.
- Double-entry accounting principles must be preserved for every balance
  mutation.
- Every balance mutation must be traceable to a command, request, or event.
- Every transaction flow must support audit logging.
- Database schema changes must support zero-downtime migration.
- Avoid distributed transactions whenever possible.
- Transaction boundaries must be explicit in application use cases.
- Idempotency is mandatory for external requests that can mutate state.
- Every external integration must define timeout, retry, idempotency, and
  failure strategy.
- Every important architectural decision requires ADR documentation.
- Critical business flows require integration tests.
- Structured logging is mandatory.
- Metrics and tracing must be supported.
- Avoid hidden magic and implicit framework behavior.
- Prefer explicit code and readability.

## Implementation Analysis

Before implementation, identify:

1. Architecture impact.
2. Domain boundaries.
3. Transactional consistency requirements.
4. Concurrency risks.
5. Locking risks.
6. Scaling bottlenecks.
7. Rollback strategy.

Before generating code, explain:

- Tradeoffs.
- Risks.
- Consistency model.
- Transaction boundaries.

## Prohibited Patterns

- Unnecessary abstraction.
- Premature CQRS or event sourcing.
- Distributed monolith patterns.
- Tight coupling between modules.
- Bypassing domain invariants.
- Mutating ledger history.
- Fake implementations without clear warning.

## Optimization Targets

Optimize for correctness, maintainability, operational stability, auditability,
explicit architecture, and production safety.

## Ledger Observability Baseline

- Ledger posting flows must emit structured audit events for:
  `POSTING_ACCEPTED`, `POSTING_REJECTED`, `DUPLICATE_REQUEST`,
  `CONFLICTING_REQUEST`, and `POSTING_FAILED`.
- Balance queries must emit the `BALANCE_CALCULATED` audit event.
- Structured audit logs must include request scope, request id, actor identity,
  actor type, transaction id when available, and sanitized safe details only.
- Metrics emitted by the ledger module currently include:
  `ledger.postings{outcome=accepted|rejected|duplicate|conflict|failed}`,
  `ledger.postings.duration`, `ledger.balance.requests{outcome=success}`, and
  `ledger.balance.duration`.
- New ledger events or metrics should use stable names because dashboards and
  alerts will bind to them.

## Verification Expectations

- Full verification for the ledger foundation includes domain, application,
  contract, and PostgreSQL-backed integration tests.
- Environment-dependent integration coverage relies on Docker/Testcontainers
  and should be called out explicitly when unavailable.
- Any remaining verification gap must be recorded in the feature quickstart or
  release notes with the exact blocked command.
