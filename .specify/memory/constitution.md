<!--
Sync Impact Report
Version change: 1.1.0 -> 1.1.1
Modified principles:
- Governance -> Governance
Added sections:
- None
Removed sections:
- None
Templates requiring updates:
- ✅ .specify/templates/plan-template.md reviewed; no changes required
- ✅ .specify/templates/spec-template.md updated
- ✅ .specify/templates/tasks-template.md updated
- ✅ .specify/templates/commands/*.md not present in this repository
- ✅ README.md reviewed; empty file, no principle references required updates
- ✅ AGENTS.md updated to reference the active constitution and ADR path
- ✅ docs/ai/*.md reviewed; no changes required
Follow-up TODOs:
- None
-->
# Ledger Core Constitution

## Core Principles

### I. Simplicity and Explicitness

Implementations MUST prefer the simplest design that satisfies the current
business requirement. New abstractions, frameworks, async flows, distributed
coordination, or CQRS splits MUST be justified by demonstrated complexity,
scalability, or reliability needs. Code MUST favor explicit control flow and
readable domain language over framework magic or hidden behavior. Fake or
placeholder implementations MUST be called out explicitly and MUST NOT be
presented as production-ready.

Rationale: enterprise systems remain maintainable when intent is visible and
complexity is paid for only when it solves a real problem.

### II. Financial Ledger Correctness

Financial correctness is non-negotiable. Ledger entries MUST be immutable after
posting; corrections MUST be represented by new reversing or adjusting entries.
Every balance mutation MUST preserve double-entry accounting, be traceable to
the initiating command/request, and produce an auditable record. Domain
invariants for accounts, balances, currency, posting state, and transfer flows
MUST NOT be bypassed by controllers, persistence adapters, scripts, or tests.

Rationale: the ledger is the system of record, so historical integrity,
traceability, and accounting balance matter more than implementation
convenience.

### III. Hexagonal Architecture and Clean Dependency Direction

Hexagonal Architecture is mandatory. Domain models and application use cases
MUST remain independent from delivery mechanisms, persistence technology,
messaging clients, and infrastructure frameworks. Adapters MAY depend on ports
and application contracts; domain and application layers MUST NOT depend on
Spring, PostgreSQL drivers, HTTP frameworks, queues, or external SDKs.

Rationale: strict dependency direction protects business behavior from
infrastructure churn and keeps core flows testable.

### IV. Pragmatic Domain-Driven Design

Domain-Driven Design MUST be applied where it clarifies business concepts,
invariants, aggregate boundaries, and ubiquitous language. Teams MUST model
critical rules in the domain or application layer rather than burying them in
controllers, persistence mappings, or scripts. DDD patterns SHOULD remain
proportional to the problem and MUST NOT introduce ceremony without a clear
business benefit.

Rationale: the domain model MUST carry the system's business meaning without
making straightforward cases harder to understand.

### V. API and Data Compatibility

Public and internal APIs MUST remain backward compatible whenever possible.
Breaking changes require a migration plan, compatibility window, and documented
consumer impact. PostgreSQL schema migrations MUST support zero-downtime
deployment, and every schema change MUST include a rollback or roll-forward
strategy. External requests that can mutate state MUST define idempotency
semantics and durable duplicate handling. CQRS MUST be limited to cases where
read/write separation has a documented operational, scaling, or modeling need.

Rationale: enterprise systems change continuously; compatibility and safe
migrations keep dependent teams and running deployments stable.

### VI. Quality, Observability, and Security

Every critical business flow MUST have integration tests that exercise the
application through real adapters or production-equivalent boundaries. Services
MUST emit structured logs, metrics, and traces for important business and
operational events, including transaction flow audit events. Authentication,
authorization, secrets handling, and input validation MUST be designed with a
security-first mindset and reviewed before production release.

Rationale: correctness, operability, and security are production requirements,
not optional hardening after implementation.

### VII. Reliability, Scalability, and Operations

Performance and scalability MUST be considered during design reviews for new
features and major changes. Async or event-driven architecture MUST only be
introduced when justified by scalability, latency, isolation, or reliability
requirements. External integrations MUST define retry, timeout, idempotency,
and failure handling strategies. Transaction boundaries MUST be explicit in
application use cases, and designs MUST identify concurrency, locking, rollback,
and scaling risks before implementation. Distributed transactions MUST document
their consistency guarantees and compensating actions.

Rationale: reliability decisions must be explicit because they shape user
experience, data correctness, and incident response.

## Architecture and Design Constraints

- Java Spring Boot is the default application framework; framework annotations
  MUST stay outside the domain model unless explicitly justified.
- PostgreSQL is the system of record; data access MUST be isolated behind
  ports/adapters and migrations MUST be deployable without stopping service.
- Infrastructure MUST be reproducible using Terraform and Docker.
- Kubernetes deployments MUST support rolling updates and graceful shutdown.
- Major architectural decisions, architecture violations, distributed
  transaction designs, and justified deviations from these principles MUST be
  recorded as Architecture Decision Records.
- New dependencies MUST include a justification covering purpose, maintenance
  risk, security posture, operational impact, and simpler alternatives.
- Modular monolith boundaries MUST remain explicit; distributed service splits
  require an ADR and a consistency/failure-mode analysis.
- Event sourcing MUST NOT be introduced unless the business and operational
  need is documented and simpler ledger-entry modeling is insufficient.

## Delivery, Operations, and Review Gates

- Design reviews MUST check architecture boundaries, API compatibility,
  migration safety, security impact, observability, and performance/scalability
  expectations before implementation begins.
- Designs for ledger mutations MUST state the consistency model, transaction
  boundary, double-entry validation, idempotency behavior, concurrency risks,
  locking risks, rollback strategy, and audit trail.
- Pull request reviews MUST verify clean layering, dependency direction,
  integration test coverage for critical flows, and rollback plans for schema
  changes.
- Database reviews MUST include migration strategy, rollback or roll-forward
  strategy, zero-downtime compatibility, index impact, and locking analysis.
- API reviews MUST include idempotency analysis, backward compatibility
  analysis, and error contract analysis.
- Event and messaging reviews MUST include delivery guarantees, retry strategy,
  deduplication strategy, and ordering requirements.
- Releases MUST include deployment and rollback notes for API, database,
  infrastructure, and external integration changes.
- Production incidents MUST result in documented postmortems with preventive
  actions and assigned owners.
- Any external integration MUST document retry policy, timeout values,
  idempotency behavior, failure modes, and observable signals.

## Governance

This constitution supersedes conflicting local conventions, feature plans, and
implementation shortcuts. Amendments require an ADR or equivalent documented
proposal that explains the change, migration impact, affected templates, and
affected runtime guidance.

Versioning follows semantic versioning:
- MAJOR for incompatible governance changes or removal/redefinition of core
  principles.
- MINOR for new principles, new mandatory sections, or materially expanded
  governance.
- PATCH for clarifications, wording changes, and non-semantic corrections.

Compliance review is mandatory during planning, design review, pull request
review, release readiness, and constitution amendments that change delivery
rules or engineering constraints. Any architecture violation requires ADR
documentation. Any new dependency requires justification. Any schema change
requires a rollback or roll-forward strategy. Any distributed transaction
requires documented consistency guarantees. Any external integration requires a
retry, timeout, idempotency, and failure handling strategy. Any constitution
amendment that changes contributor expectations MUST update the affected
templates and runtime guidance in the same change.

**Version**: 1.1.1 | **Ratified**: 2026-05-11 | **Last Amended**: 2026-05-14
