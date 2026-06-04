# Implementation Plan: Audit Trail Framework

**Branch**: `005-audit-trail-framework` | **Date**: 2026-06-04 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/005-audit-trail-framework/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Introduce a shared `audit` bounded-context module for `ledger-core` that
captures immutable audit events for business-critical state changes across
ledger, balance, and future modules. The design keeps durable audit capture
inside the modular monolith, standardizes actor and trace metadata, records
explicit ledger-transaction and idempotency linkages when they exist, stores
append-only tamper-evident audit records independently from business success
paths, supports authorized investigation queries and downstream publication,
and preserves additive extraction readiness for future multi-service
deployment.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 4.0.6, Spring Web MVC, Spring Data JPA, Bean Validation, Flyway, Actuator, PostgreSQL JDBC, Jackson JSR310, Lombok  
**Storage**: PostgreSQL as the initial durable store; Flyway-managed additive schema for audit events, integrity-link metadata, publication backlog state, retention status, and investigation indexes  
**Testing**: JUnit Platform, Spring Boot Test, PostgreSQL integration tests with Testcontainers, HTTP/consumer contract tests, and module-characterization tests for ledger/balance onboarding  
**Target Platform**: Kubernetes-hosted backend service with Docker packaging and graceful shutdown  
**Project Type**: Backend service in a modular monolith with bounded-context modules under `com.bangnk.ledgercore.ledger_core`  
**Performance Goals**: Persist 100% of valid business-critical audit events for onboarded modules; keep durable audit capture overhead low enough for financial write paths; support authorized investigation queries across correlated cross-module workflows and publication backlog recovery without duplicate event creation  
**Constraints**: Append-only and tamper-evident audit storage; zero-downtime migrations; additive backward-compatible evolution only; audit capture must not cause business transaction failure solely because downstream publication is unavailable; explicit transaction boundaries; extraction-ready module contracts; role-scoped query access; distributed tracing support; no analytics or BI scope creep  
**Scale/Scope**: Shared platform capability for ledger-foundation, account-balance-management, and future payment, transfer, settlement, and reconciliation modules; initial scope covers audit domain contracts, durable storage, authorized query contracts, publication/backlog handling, retention rules, and consumer onboarding guidance

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Simplicity and explicitness**: PASS. The design adds one bounded `audit` capability module with explicit capture, query, publication, and retention contracts instead of implicit framework interception or analytics infrastructure.
- **Financial ledger correctness**: PASS. Audit records strengthen traceability for immutable ledger history and derived balance flows without taking ownership of ledger state or weakening double-entry controls.
- **Hexagonal architecture**: PASS. Domain and application contracts stay independent from Spring, JPA, HTTP, and downstream publication implementations.
- **Pragmatic DDD**: PASS. The model focuses on audit event, actor identity, trace context, retention policy, publication attempt, and investigation view because each carries real business or compliance behavior.
- **API and data compatibility**: PASS. Schema and contract evolution are additive, legacy module-level audit/logging behavior can coexist during rollout, and new persistence is introduced through zero-downtime Flyway migration.
- **Testing**: PASS. The plan requires integration, contract, characterization, and failure-mode coverage for capture, lookup, publication backlog, retention, and investigation behavior.
- **Observability**: PASS. Capture success/fallback, publication backlog, retention transitions, integrity verification, and authorized query access are all explicit observability concerns.
- **Security**: PASS. The plan includes role-scoped access, least-privilege reads, sensitive field handling, and explicit handling for missing actor/trace values.
- **Reliability and integrations**: PASS. Durable capture, asynchronous publication recovery, and stable investigation linkage across ledger/idempotency/workflow traces are defined explicitly.
- **Transaction safety**: PASS. The design separates business-state completion, durable audit capture, and non-blocking downstream publication with explicit transactional boundaries.
- **Operations**: PASS. The design remains compatible with Docker/Kubernetes rolling deployment, additive PostgreSQL migration, and gradual module onboarding.
- **Implementation workflow readiness**: PASS. The work decomposes cleanly into domain model, storage adapter, publication adapter, query contracts, retention handling, and consumer onboarding slices suitable for worktree-first, TDD-first execution.
- **ADRs and dependencies**: PASS. Existing modular-monolith and bounded-context ADRs remain valid; no new production dependency is required by this plan.

## Project Structure

### Documentation (this feature)

```text
specs/005-audit-trail-framework/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── audit-framework-contracts.md
│   └── consumer-onboarding-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/com/bangnk/ledgercore/ledger_core/
├── LedgerCoreApplication.java
├── audit/
│   ├── adapter/
│   │   ├── in/
│   │   └── out/persistence/
│   ├── application/
│   │   ├── port/in/
│   │   └── port/out/
│   ├── config/
│   └── domain/
│       ├── model/
│       └── valueobject/
├── balance/
│   ├── adapter/
│   │   ├── in/web/
│   │   └── out/persistence/
│   ├── application/
│   │   ├── command/
│   │   ├── port/in/
│   │   ├── port/out/
│   │   └── query/
│   ├── config/
│   └── domain/
│       ├── model/
│       └── valueobject/
├── idempotency/
│   ├── adapter/
│   │   ├── in/
│   │   └── out/persistence/
│   ├── application/
│   │   ├── port/in/
│   │   └── port/out/
│   ├── config/
│   └── domain/
│       ├── model/
│       └── valueobject/
└── ledger/
    ├── adapter/
    │   ├── in/web/
    │   └── out/persistence/
    ├── application/
    │   ├── command/
    │   ├── port/in/
    │   ├── port/out/
    │   └── query/
    ├── config/
    └── domain/
        ├── model/
        └── valueobject/

src/main/resources/
└── db/migration/
    ├── V1__create_ledger_foundation.sql
    ├── V2__create_balance_management.sql
    ├── V3__create_idempotency_framework.sql
    └── V4__create_audit_trail_framework.sql

src/test/java/com/bangnk/ledgercore/ledger_core/
├── audit/
│   ├── adapter/
│   ├── application/
│   └── domain/
├── balance/
├── idempotency/
└── ledger/
```

**Structure Decision**: Introduce a new sibling `audit` bounded context under
`ledger_core`, parallel to `ledger`, `balance`, and `idempotency`. This keeps
shared audit contracts reusable for multiple modules, avoids coupling future
consumers to ledger-specific `AuditTrace` or persistence classes, and preserves
an extraction path to a dedicated audit service later without making the
current monolith depend on cross-service infrastructure now.

## Phase 0 Research Summary

Research decisions are recorded in [research.md](./research.md). They resolve
module placement, coexistence with current structured audit logging,
tamper-evident storage strategy, explicit ledger/idempotency linkage behavior,
non-blocking publication semantics, retention staging, and authorized
investigation query boundaries.

## Phase 1 Design Summary

Design artifacts are recorded in:

- [data-model.md](./data-model.md)
- [contracts/audit-framework-contracts.md](./contracts/audit-framework-contracts.md)
- [contracts/consumer-onboarding-contract.md](./contracts/consumer-onboarding-contract.md)
- [quickstart.md](./quickstart.md)

## Post-Design Constitution Check

- **Simplicity and explicitness**: PASS. The design uses one bounded capability module with explicit capture, publish, query, and retention ports instead of hidden interception or analytics pipelines.
- **Financial ledger correctness**: PASS. Ledger transaction linkage is explicit, but the audit framework remains evidence-oriented and never becomes the source of truth for ledger state.
- **Hexagonal architecture**: PASS. Business modules integrate through stable ports and value objects while adapters isolate persistence, publication, and query transport concerns.
- **Pragmatic DDD**: PASS. The selected model stays focused on audit event, actor identity, trace context, integrity chain, publication attempt, and retention profile because those are the minimal concepts needed for compliance-grade behavior.
- **API and data compatibility**: PASS. Existing ledger structured audit logging and current module behavior can coexist during rollout; new schema and contracts are additive.
- **Testing**: PASS. The quickstart and contracts require capture, failure-path, publication backlog, retention, integrity, and cross-module linkage verification through production-equivalent adapters.
- **Observability**: PASS. Audit capture, publication deferment, retention transitions, integrity-check failures, and query access are all observable by design.
- **Security**: PASS. The contracts define role-scoped access, sensitive field controls, and explicit handling of known versus unavailable actor/trace data.
- **Reliability and integrations**: PASS. The design documents safe behavior when publication fails, when trace data is missing, and when events must be correlated across modules or later services.
- **Transaction safety**: PASS. Business completion, durable audit capture, and downstream publication are separated by explicit transactional boundaries that support fail-open publication behavior but durable evidence capture.
- **Operations**: PASS. The design supports rolling deployment, additive Flyway migrations, gradual consumer adoption, and backlog replay without downtime.
- **Implementation workflow readiness**: PASS. The work splits naturally into domain, persistence, publication, query, retention, and onboarding streams appropriate for the required worktree/TDD/subagent/review workflow.
- **ADRs and dependencies**: PASS. Existing ADRs remain valid and no new dependency is required; a future extracted audit service or cryptographic signature infrastructure would require separate ADR review if introduced later.

## Complexity Tracking

No constitution violations require justification.
