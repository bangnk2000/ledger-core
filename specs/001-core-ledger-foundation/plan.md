# Implementation Plan: Core Ledger Foundation

**Branch**: `001-core-ledger-foundation` | **Date**: 2026-05-11 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-core-ledger-foundation/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Establish the immutable double-entry ledger foundation for `ledger-core`: callers submit idempotent posting requests containing debit and credit entries, the system validates balance and input invariants, records accepted transactions and entries atomically, rejects invalid or conflicting retries without partial visibility, derives balances from posted entries, and emits structured audit/operational signals. The technical approach is a Spring Boot 4 / Java 21 modular-monolith implementation using hexagonal architecture, tactical DDD, PostgreSQL transactions, Flyway migrations, JPA persistence adapters, validation at the API boundary, and integration tests through HTTP/application/persistence boundaries.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 4.0.6, Spring Web MVC, Spring Data JPA, Bean Validation, Flyway, Actuator, PostgreSQL JDBC, Lombok  
**Storage**: PostgreSQL as system of record; Flyway-managed schema for ledger transactions, ledger entries, and request idempotency records  
**Testing**: JUnit Platform, Spring Boot Test slices, integration tests through production-equivalent adapters; Testcontainers is planned for PostgreSQL integration coverage and requires dependency addition during implementation  
**Target Platform**: Kubernetes-hosted backend service with Docker packaging and rolling deployment compatibility  
**Project Type**: Backend service in a modular monolith  
**Performance Goals**: Support at least 100 simultaneous valid posting submissions with every accepted transaction balanced; calculate a single account balance over at least 10,000 posted entries within 2 seconds in standard verification environments  
**Constraints**: Immutable ledger entries, mandatory double-entry validation, explicit transaction boundaries, backward-compatible additive APIs, zero-downtime database migration, structured audit/operational events, no event sourcing, no distributed transaction, no Kafka dependency for this feature  
**Scale/Scope**: Single-currency foundation; internal authorized callers; ledger module first; accounts are referenced by stable account identifiers but account lifecycle is outside this feature; read optimization, settlement, external provider integration, distributed event processing, and multi-currency are out of scope

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Simplicity and explicitness**: PASS. The design uses one deployable Spring Boot application, one PostgreSQL database, explicit application use cases, and no new async, distributed, event-sourcing, or full CQRS infrastructure.
- **Financial ledger correctness**: PASS. Posting requires at least one debit and one credit, exact debit/credit equality, immutable entries, correction by new postings, request traceability, and audit events.
- **Hexagonal architecture**: PASS. Domain value objects and aggregates remain framework-free; controllers and JPA mappings live in adapters; application services own use cases and transaction boundaries.
- **Pragmatic DDD**: PASS. Ledger Transaction, Ledger Entry, Posting Request, Account Balance, and Audit Trace are modeled where they carry accounting invariants.
- **API and data compatibility**: PASS. New HTTP endpoints are additive. Flyway migrations use create-only expand steps and avoid destructive schema changes.
- **Testing**: PASS. Critical flows require integration tests for balanced posting, rejection without partial records, immutability, idempotent retry, conflict retry, concurrency, and balance derivation.
- **Observability**: PASS. Structured logs, metrics, and traces are required for accepted postings, rejected postings, duplicate requests, conflicts, failures, and balance query behavior.
- **Security**: PASS. Authorization is delegated to the existing security model, while this feature validates inputs, avoids leaking internals in errors, and records actor/request trace details.
- **Reliability and integrations**: PASS. Mutating requests require idempotency keys with stable duplicate outcomes; no external integration or async delivery is introduced in this feature.
- **Transaction safety**: PASS. Posting and idempotency persistence occur inside one explicit database transaction. Balance reads use committed posted entries only.
- **Operations**: PASS. The design preserves Docker/Kubernetes rolling update compatibility and uses Flyway for reproducible migration.
- **ADRs and dependencies**: PASS. Existing ADRs support modular monolith, hexagonal architecture, tactical DDD, lightweight CQRS, PostgreSQL, and Flyway. Testcontainers is the only planned new dependency and is limited to tests.

## Project Structure

### Documentation (this feature)

```text
specs/001-core-ledger-foundation/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ledger-api.openapi.yaml
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/com/bangnk/ledgercore/ledger_core/
├── LedgerCoreApplication.java
└── ledger/
    ├── domain/
    │   ├── model/
    │   ├── valueobject/
    │   └── service/
    ├── application/
    │   ├── command/
    │   ├── query/
    │   ├── port/in/
    │   └── port/out/
    ├── adapter/
    │   ├── in/web/
    │   └── out/persistence/
    └── config/

src/main/resources/
└── db/migration/
    └── V1__create_ledger_foundation.sql

src/test/java/com/bangnk/ledgercore/ledger_core/
├── ledger/domain/
├── ledger/application/
└── ledger/adapter/
```

**Structure Decision**: Implement a single `ledger` module inside the existing Spring Boot application package. Within that module, use hexagonal package boundaries: domain models/value objects have no Spring or JPA dependencies, application commands/queries and ports orchestrate use cases, web and persistence adapters translate external concerns, and configuration wires adapters to ports. This keeps the modular monolith simple while leaving clear boundaries for later account, transfer, transaction, and reconciliation modules from ADR-001.

## Phase 0 Research Summary

Research decisions are recorded in [research.md](./research.md). All planning unknowns have been resolved from the repository, specification, constitution, and ADRs.

## Phase 1 Design Summary

Design artifacts are recorded in:

- [data-model.md](./data-model.md)
- [contracts/ledger-api.openapi.yaml](./contracts/ledger-api.openapi.yaml)
- [quickstart.md](./quickstart.md)

## Post-Design Constitution Check

- **Simplicity and explicitness**: PASS. Contracts and data model stay within the foundation scope and avoid premature distributed/event-sourced architecture.
- **Financial ledger correctness**: PASS. Data model encodes immutable entries, atomic transaction grouping, balancing rules, correction-by-new-posting, idempotency, and audit trace retention.
- **Hexagonal architecture**: PASS. API contracts map to inbound adapters and application ports; persistence details remain outbound adapter concerns.
- **Pragmatic DDD**: PASS. Aggregates and value objects are limited to invariants required by the feature.
- **API and data compatibility**: PASS. The API contract is additive and versionable; migration approach is expand-only for the initial ledger tables.
- **Testing**: PASS. Quickstart and later tasks must include integration coverage for critical business flows and PostgreSQL behavior.
- **Observability**: PASS. Audit/operational event categories are part of the contract and design.
- **Security**: PASS. Actor identity and authorization concerns are explicit, with sensitive details excluded from error responses.
- **Reliability and integrations**: PASS. Stable idempotency and duplicate/conflict behavior are specified without external dependencies.
- **Transaction safety**: PASS. Atomic posting and idempotency are one transaction boundary; balance reads derive from committed posted entries.
- **Operations**: PASS. Flyway and rolling deployment constraints are preserved.
- **ADRs and dependencies**: PASS. No ADR violation found. Testcontainers requires dependency justification in implementation tasks because it is a test-only dependency.

## Complexity Tracking

No constitution violations require justification.
