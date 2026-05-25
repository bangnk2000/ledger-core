# Implementation Plan: Ledger Technical Debt Compliance

**Branch**: `003-tech-debt-compliance` | **Date**: 2026-05-19 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-tech-debt-compliance/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Bring `ledger-core` back to a passing SonarQube/SonarCloud quality gate and
resolve critical Qodana findings, using a safe, incremental refactor strategy
that prioritizes transaction-critical and replay-sensitive modules first.

Delivery is organized into small waves (one module group + one risk category
per wave) with strict separation between:

- transaction-boundary refactors
- concurrency refactors
- replay/determinism refactors

Each wave is correctness-first: replay determinism, explicit transaction
integrity, and balance correctness must remain unchanged, and validation is
mandatory before merge for any change in transaction-critical modules.

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 4.0.6, Spring Web MVC, Spring Data JPA, Bean Validation, Flyway, Actuator, PostgreSQL JDBC, Jackson JSR310, Lombok; static analysis via Sonar (Gradle `org.sonarqube` plugin) and Qodana (JetBrains Qodana JVM Community linter)
**Storage**: PostgreSQL (Flyway-managed migrations under `src/main/resources/db/migration/`)
**Testing**: JUnit Platform, Spring Boot Test, PostgreSQL integration tests with Testcontainers, contract tests, concurrency tests (where applicable)
**Target Platform**: Kubernetes-hosted backend service with Docker packaging and rolling deployment compatibility
**Project Type**: backend service in a modular monolith with bounded-context separation inside the `ledger` module
**Performance Goals**: No runtime behavior regression; keep CI quality gates within a reasonable window (target: <= 15 min for `./gradlew build` + Sonar analysis, <= 20 min for Qodana scan on standard runners)
**Constraints**: No breaking API changes; no large-scale rewrite; preserve replay determinism, transaction integrity, and balance correctness; explicit transaction boundaries only; zero-downtime migrations; strict hexagonal dependency direction
**Scale/Scope**: Scope focuses on in-scope modules failing quality gates, with priority order: transaction-critical and replay-sensitive modules first (ledger foundation + balance bounded context), then lower-risk support modules and tooling

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Simplicity and explicitness**: PASS. Refactors are incremental, wave-based, and avoid new frameworks; changes remain explicit and localized.
- **Financial ledger correctness**: PASS. Ledger entries remain immutable; any change affecting posting, reservations, or balance computation must preserve double-entry invariants and audit traceability.
- **Hexagonal architecture**: PASS. Refactors target duplication/complexity while preserving dependency direction; framework concerns remain in adapters/config.
- **Pragmatic DDD**: PASS. Domain invariants stay enforced in domain/application layers; refactors do not move behavior into controllers or persistence mappings.
- **API and data compatibility**: PASS. No breaking API changes; schema changes (if any) remain additive/expand-compatible with zero-downtime migration guidance.
- **Testing**: PASS. Strategy mandates characterization, regression, integration, and concurrency validation; transaction-critical modules require replay/concurrency checkpoints before merge.
- **Observability**: PASS. Logging cleanup is constrained to format/structure and must preserve audit event semantics and metrics naming.
- **Security**: PASS. Refactors should not alter authn/z; any security-sensitive touched code requires explicit review checklist coverage.
- **Reliability and integrations**: PASS. Retry/timeout/idempotency semantics are preserved; refactors must not introduce implicit retries or hidden async flows.
- **Transaction safety**: PASS. Transaction boundaries remain explicit; transaction-boundary refactors are isolated into dedicated waves with targeted validation.
- **Operations**: PASS. CI and quality gate policy updates are designed to be stable and non-flaky; deployment behavior remains unchanged.
- **Implementation workflow readiness**: PASS. Work proceeds in small waves and follows the required workflow (worktree -> TDD -> review -> finish-branch) for any task-list execution.
- **ADRs and dependencies**: PASS. No new production dependencies required for the initial waves; any major deviation triggers ADR capture under `docs/ADR/`.

## Project Structure

### Documentation (this feature)

```text
specs/003-tech-debt-compliance/
├── plan.md                                # This file (/speckit-plan command output)
├── research.md                            # Phase 0 output (/speckit-plan command)
├── data-model.md                          # Phase 1 output (/speckit-plan command)
├── quickstart.md                          # Phase 1 output (/speckit-plan command)
├── contracts/                             # Phase 1 output (/speckit-plan command)
│   ├── quality-gate-policy.md
│   ├── module-classification.md
│   ├── module-by-module-execution-plan.md
│   ├── refactor-backlog.md
│   └── risk-matrix.md
└── tasks.md                               # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
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
    │   ├── adapter/in/web/
    │   ├── adapter/out/persistence/
    │   ├── application/           # protected-write + replay ordering + idempotency
    │   ├── domain/
    │   └── config/
    ├── domain/
    └── config/

src/main/resources/db/migration/

src/test/java/com/bangnk/ledgercore/ledger_core/
└── ledger/
    ├── adapter/
    ├── application/
    ├── balance/
    └── domain/

.github/workflows/
├── sonarqube.yml
└── qodana_code_quality.yml

qodana.yaml
```

**Structure Decision**: This feature is documentation-driven and enforcement-driven:
the contracts and execution plan live under `specs/003-tech-debt-compliance/`
and govern incremental refactors applied in-place to `ledger` and
`ledger/balance` code, with no API breakage and strict correctness validation.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| N/A | N/A | N/A |
