# Implementation Plan: Idempotency Framework

**Branch**: `004-idempotency-framework` | **Date**: 2026-06-04 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/004-idempotency-framework/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Add a reusable `idempotency` capability to `ledger-core` as a shared
bounded-context module that can protect synchronous external requests and
asynchronous workflows from duplicate side effects. The design keeps the module
inside the modular monolith, exposes explicit application ports instead of
framework-coupled helpers, fingerprints canonical business-intent objects using
normalized material fields only, uses durable scope-aware idempotency records
plus an explicit `RECEIVED -> CLAIMED -> PROCESSING -> COMPLETED | REJECTED |
INDETERMINATE` lifecycle, applies a replay-window-then-tombstone retention
policy, supports concurrent duplicate requests across horizontally scaled
application instances, and rolls out additively alongside the existing
ledger-specific and balance-specific idempotency implementations.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 4.0.6, Spring Web MVC, Spring Data JPA, Bean Validation, Flyway, Actuator, PostgreSQL JDBC, Jackson JSR310, Lombok  
**Storage**: PostgreSQL as the initial durable store; Flyway-managed schema for generic idempotency records, canonical fingerprint metadata, replay outcome metadata, replay-window and tombstone-retention timestamps, and audit fields  
**Testing**: JUnit Platform, Spring Boot Test, PostgreSQL integration tests with Testcontainers, HTTP/consumer contract tests, concurrency integration tests, duplicate/replay characterization tests  
**Target Platform**: Kubernetes-hosted backend service with Docker packaging and rolling deployment compatibility  
**Project Type**: Backend service in a modular monolith with bounded-context modules under `com.bangnk.ledgercore.ledger_core`  
**Performance Goals**: Prevent duplicate side effects for 100% of verified identical retries; resolve duplicate detection deterministically under at least 100 concurrent duplicate attempts for the same scope/key in verification scenarios; keep idempotency lookup-and-claim overhead low enough that protected request handling remains practical for financial transaction flows  
**Constraints**: Additive backward-compatible rollout only; zero-downtime migrations; explicit transaction boundaries; horizontally safe duplicate detection; canonicalization must exclude transport metadata; expiration must use replay-window then tombstone semantics; storage implementation independence at the application/domain contract level; auditable lifecycle history; no framework leakage into domain model; no forced migration of existing ledger or balance idempotency tables in the first rollout  
**Scale/Scope**: Shared platform capability for future ledger, transaction, webhook, integration, payment, and background-job flows; initial scope defines the framework module, persistence contract, lifecycle model, consumer contracts, and onboarding path; payment-specific rules, business-specific settlement behavior, and API gateway mechanics remain out of scope

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Simplicity and explicitness**: PASS. The design introduces one focused bounded-context module inside the monolith instead of a distributed coordinator, broker, or framework-heavy interception layer.
- **Financial ledger correctness**: PASS. The framework blocks duplicate side effects and preserves auditability without mutating immutable ledger history or bypassing existing accounting controls.
- **Hexagonal architecture**: PASS. Domain and application contracts remain independent from Spring, JPA, HTTP, and PostgreSQL details; adapters own persistence and delivery concerns.
- **Pragmatic DDD**: PASS. The module models only the aggregates and value objects needed for key scoping, canonical fingerprint comparison, lifecycle tracking, retention handling, and replay decisions.
- **API and data compatibility**: PASS. The rollout is additive, existing consumer contracts remain intact, and any new schema is introduced with coexistence rather than in-place replacement.
- **Testing**: PASS. The plan requires integration, contract, characterization, and concurrency coverage for duplicate suppression, replay, conflict, expiration, and retention-window behavior.
- **Observability**: PASS. Lifecycle transitions, duplicate-in-progress events, replay-window expiry, tombstone retention, conflicts, and indeterminate outcomes are all explicitly observable.
- **Security**: PASS. The design requires key validation, scoped access to inspection/replay functions, canonicalization that ignores transport-only metadata, and sanitization of replayed outcome data.
- **Reliability and integrations**: PASS. Retry-safe claim/replay semantics, conflict handling, replay-window/tombstone expiration behavior, and indeterminate-outcome recovery are explicit.
- **Transaction safety**: PASS. Key claim, protected work, and finalization are separated by explicit transactional boundaries with documented concurrency guarantees.
- **Operations**: PASS. The design stays compatible with Docker/Kubernetes rolling deployments, additive Flyway migrations, and gradual consumer onboarding.
- **Implementation workflow readiness**: PASS. The work can be decomposed into worktree-first, TDD-driven tasks across domain, persistence, integration, retention cleanup, and consumer onboarding slices.
- **ADRs and dependencies**: PASS. Existing ADRs for modular monolith and balance bounded-context isolation remain consistent; no new production dependency is required by this plan.

## Project Structure

### Documentation (this feature)

```text
specs/004-idempotency-framework/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── consumer-onboarding-contract.md
│   └── idempotency-module-contracts.md
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/com/bangnk/ledgercore/ledger_core/
├── LedgerCoreApplication.java
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
│   │   ├── command/
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
    └── V3__create_idempotency_framework.sql

src/test/java/com/bangnk/ledgercore/ledger_core/
├── balance/
├── idempotency/
│   ├── adapter/
│   ├── application/
│   └── domain/
└── ledger/
```

**Structure Decision**: Introduce a new sibling `idempotency` bounded-context
subtree under `ledger_core`, rather than embedding another specialized
implementation inside `ledger` or `balance`. This keeps the capability reusable
for multiple modules, preserves strict dependency direction, and allows ledger
and balance to adopt it gradually through explicit ports and anti-corruption
translation instead of direct table or model sharing.

## Phase 0 Research Summary

Research decisions are recorded in [research.md](./research.md). They resolve
module placement, canonical fingerprint rules, lifecycle state modeling,
concurrent claim semantics, replay-window/tombstone expiration strategy, and
coexistence with existing ledger/balance idempotency implementations.

## Phase 1 Design Summary

Design artifacts are recorded in:

- [data-model.md](./data-model.md)
- [contracts/idempotency-module-contracts.md](./contracts/idempotency-module-contracts.md)
- [contracts/consumer-onboarding-contract.md](./contracts/consumer-onboarding-contract.md)
- [quickstart.md](./quickstart.md)

## Implementation Status

- Atomic first-claim acquisition is implemented in the PostgreSQL adapter with
  `INSERT ... ON CONFLICT DO NOTHING`.
- Canonical fingerprint translators are implemented for the current ledger and
  balance onboarding path and explicitly exclude transport-only metadata from
  fingerprint material.
- Replay-window, tombstone, and purge cleanup flows are implemented and covered
  by focused domain, application, and PostgreSQL integration tests.

## Post-Design Constitution Check

- **Simplicity and explicitness**: PASS. The design uses one bounded capability module, explicit claim/finalize contracts, and additive persistence instead of hidden framework interception or distributed coordination.
- **Financial ledger correctness**: PASS. Existing immutable ledger authority is preserved, and duplicate suppression is framed as a protective capability for financial side effects rather than a replacement for ledger invariants.
- **Hexagonal architecture**: PASS. Consumer modules integrate through ports and value objects; adapters isolate JPA, SQL, HTTP, and operational cleanup concerns.
- **Pragmatic DDD**: PASS. The chosen model focuses on Idempotency Record, Scope, canonical Fingerprint, Replay Outcome, retention status, and Lifecycle Transition because each carries real behavior and invariants.
- **API and data compatibility**: PASS. Existing ledger and balance behavior remains untouched until consumer-specific migration tasks opt in; the new schema is additive and coexistence-safe.
- **Testing**: PASS. The quickstart and contracts require duplicate, replay, conflict, expiration, retention-window, and concurrency verification through production-equivalent adapters.
- **Observability**: PASS. Duplicate claims, replayed results, replay-window expiry, tombstone retention, conflicts, long-running processing, cleanup actions, and indeterminate recovery are all part of the module contract.
- **Security**: PASS. The contracts limit replay data exposure, require scope-aware access, and treat key/fingerprint validation plus transport-metadata exclusion as first-class checks.
- **Reliability and integrations**: PASS. The module documents safe retry outcomes, concurrent duplicate behavior, replay-window/tombstone expiration semantics, and failure handling for uncertain downstream effects.
- **Transaction safety**: PASS. Claim acquisition, business execution, and finalization remain explicit and separately reviewable; no implicit transaction-spanning magic is introduced.
- **Operations**: PASS. The design supports rolling deployment, online schema expansion, staged consumer adoption, and background cleanup without downtime.
- **Implementation workflow readiness**: PASS. The work decomposes cleanly into domain model, persistence adapter, integration contracts, migration, retention cleanup, and consumer adoption tasks suitable for required worktree/TDD/subagent/review workflow.
- **ADRs and dependencies**: PASS. Existing ADRs remain valid and no new dependency is required; a future cross-service idempotency coordinator would require separate ADR treatment if introduced later.

## Complexity Tracking

No constitution violations require justification.
