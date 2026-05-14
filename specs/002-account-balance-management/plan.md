# Implementation Plan: Account Balance Management

**Branch**: `002-account-balance-management` | **Date**: 2026-05-14 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-account-balance-management/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Add the `account balance management` bounded context to `ledger-core` as a
projection-based module that keeps immutable ledger history authoritative for
finalized accounting state while owning current balance state, reservations,
rebuilds, and reconciliation. The implementation remains inside the modular
monolith, uses explicit bounded-context contracts instead of direct table
coupling, protects spend and reservation writes with explicit transactional
boundaries and concurrency control, supports deterministic replay through
controlled ledger exports, and preserves future extraction into a dedicated
service or worker without redesigning ledger history.

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 4.0.6, Spring Web MVC, Spring Data JPA, Bean Validation, Flyway, Actuator, PostgreSQL JDBC, Jackson JSR310, Lombok
**Storage**: PostgreSQL as system of record; Flyway-managed schema for balance state, reservations, rebuild checkpoints, reconciliation records, and replay/export metadata
**Testing**: JUnit Platform, Spring Boot Test, PostgreSQL integration tests with Testcontainers, HTTP contract tests, concurrency integration tests, deterministic replay verification tests
**Target Platform**: Kubernetes-hosted backend service with Docker packaging and rolling deployment compatibility
**Project Type**: Backend service in a modular monolith with bounded-context separation inside the `ledger` module
**Performance Goals**: Support at least 100 simultaneous overlapping debit or transfer attempts with zero overspend violations; serve single-account current balance snapshots with p99 latency at or below 250 ms in standard verification environments; rebuild at least 10,000 balance-affecting history records per minute in standard verification environments
**Constraints**: Immutable ledger authority, mandatory double-entry correctness, explicit transaction boundaries, protected-write failure isolation, contract-only integration with controlled replay exports, additive backward-compatible contract evolution, zero-downtime schema rollout, deterministic replayability, no direct table coupling, no distributed transaction, no event sourcing
**Scale/Scope**: Single-currency balance management for internal authorized callers; balance state, reservations, balance queries, rebuilds, reconciliation, and recovery are in scope; multi-currency, foreign exchange, external settlement networks, and independent deployment are out of scope for this slice

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Simplicity and explicitness**: PASS. The design stays inside one deployable application and one PostgreSQL database, uses explicit contracts instead of async infrastructure by default, and avoids premature service extraction.
- **Financial ledger correctness**: PASS. Immutable ledger entries remain authoritative, balance mutations stay traceable, reservations are explicit, and protected writes fail closed when balance guarantees cannot be upheld.
- **Hexagonal architecture**: PASS. Balance domain/application logic remains framework-free; web and persistence details stay in adapters; ledger-foundation exposes immutable contracts rather than persistence internals.
- **Pragmatic DDD**: PASS. Balance State, Funds Reservation, Rebuild Job, and Reconciliation Record are modeled because they carry real invariants and lifecycle rules.
- **API and data compatibility**: PASS. All new APIs and replay exports are additive and versioned; schema changes are expand-compatible and isolated by bounded context.
- **Testing**: PASS. Critical flows require PostgreSQL-backed integration tests, contract tests, concurrency verification, and deterministic replay tests.
- **Observability**: PASS. Structured logs, metrics, traces, lag signals, rebuild progress, and discrepancy records are part of the design.
- **Security**: PASS. Authorization boundaries for balance reads, writes, rebuilds, and recovery are explicit; error contracts exclude secrets and infrastructure details.
- **Reliability and integrations**: PASS. Idempotency, retry rules, lock timeout behavior, replay safety, and failure isolation are specified without distributed transactions.
- **Transaction safety**: PASS. Protected writes define explicit transaction boundaries, deterministic lock ordering, retry limits, and rollback expectations.
- **Operations**: PASS. Docker/Kubernetes rollout stays compatible with additive migrations, dual-read readiness, and module-local recovery tooling.
- **ADRs and dependencies**: PASS. Existing ADRs for modular monolith, hexagonal architecture, tactical DDD, PostgreSQL, Flyway, and lightweight CQRS are consistent with the feature. No new production dependency is required by the plan.

## Project Structure

### Documentation (this feature)

```text
specs/002-account-balance-management/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── balance-api.openapi.yaml
│   └── module-interaction-contracts.md
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/com/bangnk/ledgercore/ledger_core/
├── LedgerCoreApplication.java
└── ledger/
    ├── adapter/
    │   ├── in/web/
    │   └── out/persistence/
    ├── application/
    │   ├── command/
    │   ├── port/in/
    │   ├── port/out/
    │   └── query/
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
    ├── config/
    └── domain/
        ├── model/
        └── valueobject/

src/main/resources/
└── db/migration/
    ├── V1__create_ledger_foundation.sql
    └── V2__create_balance_management.sql

src/test/java/com/bangnk/ledgercore/ledger_core/
└── ledger/
    ├── adapter/
    ├── application/
    ├── balance/
    │   ├── adapter/
    │   ├── application/
    │   └── domain/
    └── domain/
```

**Structure Decision**: Preserve the existing `ledger` foundation packages as
the authoritative accounting bounded context and introduce a sibling
`ledger/balance` subtree for account balance management. This keeps the current
modular-monolith deployment simple while making dependency direction explicit:
`balance` may depend only on immutable ledger contracts and controlled replay
exports, and ledger-foundation must not depend on balance internals.

## Phase 0 Research Summary

Research decisions are recorded in [research.md](./research.md). They resolve
the implementation shape for bounded-context separation, contract ownership,
replay exports, failure isolation, concurrency control, and schema rollout.

## Phase 1 Design Summary

Design artifacts are recorded in:

- [data-model.md](./data-model.md)
- [contracts/balance-api.openapi.yaml](./contracts/balance-api.openapi.yaml)
- [contracts/module-interaction-contracts.md](./contracts/module-interaction-contracts.md)
- [quickstart.md](./quickstart.md)

## Post-Design Constitution Check

- **Simplicity and explicitness**: PASS. The design uses one deployment unit, one database, explicit write boundaries, and replay exports rather than a new broker or distributed topology.
- **Financial ledger correctness**: PASS. Ledger-foundation remains immutable and authoritative; balance-management only derives and protects current state, reservations, and reconciliation.
- **Hexagonal architecture**: PASS. Cross-context interaction is through ports and contracts; neither domain model depends on Spring, JPA, or web concerns.
- **Pragmatic DDD**: PASS. The feature models only the aggregates and lifecycle objects required by protected writes, balance reads, and recovery workflows.
- **API and data compatibility**: PASS. Contracts are additive and versioned, replay exports are replay-safe, and migrations stay zero-downtime compatible.
- **Testing**: PASS. Quickstart and contracts require integration, contract, concurrency, and replay coverage for every critical flow.
- **Observability**: PASS. The design defines lag signals, lock contention metrics, reconciliation discrepancy records, rebuild progress, and failure-isolation observability.
- **Security**: PASS. Authorization is scoped by operation class and account scope; recovery and rebuild actions are separated from normal read/write permissions.
- **Reliability and integrations**: PASS. Protected-write isolation, retry policy, replay safety, checkpointed rebuilds, and independent recovery tooling are explicit.
- **Transaction safety**: PASS. Transaction boundaries, lock ordering, stale-write detection, rollback behavior, and failure modes are documented in the contracts and data model.
- **Operations**: PASS. Deployment remains rolling-update safe, rebuild/reconciliation are isolated, and additive migration sequencing is documented.
- **ADRs and dependencies**: PASS. No new ADR is required by the chosen design; if extraction or broker-backed event delivery is introduced later, that future step will require ADR coverage.

## Complexity Tracking

No constitution violations require justification.
