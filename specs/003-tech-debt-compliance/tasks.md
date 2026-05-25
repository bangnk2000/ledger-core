# Tasks: Ledger Technical Debt Compliance

**Input**: Design documents from `/specs/003-tech-debt-compliance/`
**Prerequisites**: [plan.md](./plan.md) (required), [spec.md](./spec.md) (required), [research.md](./research.md), [data-model.md](./data-model.md), `contracts/`

**Tests**: Integration tests are REQUIRED for every critical business flow. Characterization, regression, and concurrency tests MUST be included when needed to validate determinism, transaction integrity, idempotency, adapters, and regressions identified by Sonar/Qodana.

**Execution Workflow**: Every task list MUST be executed using the mandatory workflow order from the constitution: worktree -> TDD (red-green-refactor) -> subagent-driven execution -> code review -> finish-branch.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions wherever a file is modified

## Phase 0: Worktree and Verification Harness

**Purpose**: Establish isolated execution, repeatable verification commands, and baseline artifacts before any refactor/CI tightening.

- [x] T000 Record the chosen worktree location and wave branch strategy in specs/003-tech-debt-compliance/contracts/module-by-module-execution-plan.md (acceptance: worktree path documented, branch strategy documented, wave rollback strategy documented)
- [x] T001 Capture the authoritative verification commands for this feature in specs/003-tech-debt-compliance/quickstart.md (build, focused tests, Sonar task, Qodana scan) (acceptance: canonical command block includes build, focused replay/concurrency tests, Sonar task, and Qodana scan baseline command)
- [x] T002 Define baseline replay/concurrency “must-run” test list in specs/003-tech-debt-compliance/contracts/quality-gate-policy.md (transaction-critical override section) (acceptance: override section lists must-run commands, merge-blocking behavior, and evidence requirement)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Ensure report ingestion and local tooling is deterministic enough to support incremental waves.

- [x] T003 Ensure tools/sonar/fetch-sonar-report.sh creates build/reports/ and documents required env vars in tools/sonar/README.md (acceptance: script ensures report directory exists, validates token, and README lists required/optional env vars)
- [x] T004 [P] Add a skeleton Qodana baseline file placeholder at tools/qodana/qodana.sarif.json (tracked) with instructions in tools/qodana/README.md for updating it safely (acceptance: placeholder SARIF file is tracked as scaffold only; README states real baseline is established in T007 and defines safe update workflow)
- [x] T005 Add coding + architecture refactor standards to specs/003-tech-debt-compliance/contracts/coding-standards.md (hexagonal dependency rules, transaction-boundary rules, replay determinism rules, concurrency rules) (acceptance: standards document includes all four rule groups and wave hygiene guidance)

**Checkpoint**: Local workflow can fetch/record analysis snapshots and has an explicit standards doc for refactor hygiene.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the “triage -> wave -> validation” loop and ensure transaction-critical modules have mandatory validation gates.

**CRITICAL**: No refactor waves start until this phase is complete.

- [x] T006 Pull a Sonar snapshot into build/reports/sonar-report.json using tools/sonar/fetch-sonar-report.sh (update docs if token/host differs)
- [ ] T007 Pull a Qodana snapshot (CI artifact or local) and record the baseline file path decision in specs/003-tech-debt-compliance/contracts/quality-gate-policy.md (ABORTED by user on 2026-05-19)
- [x] T008 Normalize current Sonar/Qodana critical findings into specs/003-tech-debt-compliance/contracts/refactor-backlog.md with stable IDs, modules, categories, risk, and acceptance checks (completed for Sonar scope after Qodana abort)
- [x] T009 Update specs/003-tech-debt-compliance/contracts/module-classification.md with any newly discovered transaction boundaries or replay-sensitive paths surfaced by triage (completed for Sonar scope after Qodana abort)
- T006-T009 status note (2026-05-19, resumed with local artifact update):
  - T006: DONE. `build/reports/sonar-report.json` is present and parseable (`total=50`, `critical_count=2`, `major_count=48` via `node` parse command).
  - T007: ABORTED by user request on 2026-05-19.
  - T008: DONE (Sonar scope). Sonar critical findings normalized into `contracts/refactor-backlog.md`.
  - T009: DONE (Sonar scope). Classification triage performed from available Sonar critical findings.
  - Scope rule after abort: this execution track is Sonar-only; Qodana ingestion tasks remain intentionally open/aborted.
- [X] T010 Create characterization tests for replay ordering in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceReplayOrderingServiceTest.java
- [X] T011 Create a deterministic replay “golden dataset” fixture for balance rebuild in src/test/resources/ledger/replay/ (document format in specs/003-tech-debt-compliance/quickstart.md)
- [X] T012 Add a replay determinism integration/characterization test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReplayDeterminismIntegrationTest.java that replays the same fixture twice and asserts identical persisted state
- [X] T013 Add concurrency characterization coverage for protected writes by extending src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReservationConcurrencyIntegrationTest.java with deterministic assertions (no flake, bounded time)

**Checkpoint**: A backlog exists, critical module boundaries are explicit, and replay/concurrency validation exists for transaction-critical modules.

---

## Phase 3: User Story 1 - Restore Quality Gate Compliance (Priority: P1) MVP

**Goal**: Sonar quality gate enforcement is correct/stable and Qodana blocks new critical findings with time-bound exceptions, without breaking CI reliability.

**Independent Test**: Run CI quality checks locally where possible and confirm the workflows are correctly configured to run analysis and fail appropriately.

### Tests for User Story 1

- [x] T014 [P] [US1] Add workflow-level validation tests (lint/yaml sanity) by adding a minimal .github/workflows/ci-sanity.yml that runs on pull_request and validates workflow YAML files compile (no runtime secrets required)

### Implementation for User Story 1 (Wave W0)

- [x] T015 [US1] Fix Sonar workflow to run analysis explicitly (replace duplicate ./gradlew build) in .github/workflows/sonarqube.yml
- [x] T016 [US1] Ensure Sonar workflow passes auth properties to Gradle and waits for quality gate (sonar.qualitygate.wait=true) in .github/workflows/sonarqube.yml
- [x] T017 [US1] Fix Qodana workflow checkout for push events (do not reference github.event.pull_request.head.sha on push) in .github/workflows/qodana_code_quality.yml
- [x] T018 [US1] Enable PR-diff behavior for Qodana (pr-mode: true on pull_request, false on push) in .github/workflows/qodana_code_quality.yml
- [x] T019 [US1] Enforce “block new critical” using Qodana baseline + failure threshold (args: --baseline tools/qodana/qodana.sarif.json) and document the update process in tools/qodana/README.md
- [x] T020 [US1] Define and enforce the time-bound exception workflow (record + expiry) by updating specs/003-tech-debt-compliance/contracts/quality-gate-policy.md and adding a CI check step placeholder in .github/workflows/qodana_code_quality.yml
- [x] T021 [US1] Validate Gradle Sonar task naming and wire the correct task invocation (./gradlew sonar) in .github/workflows/sonarqube.yml

**Checkpoint**: W0 complete; CI analysis runs deterministically and blocks merges on quality gate failure and new critical findings (with documented exception policy).

---

## Phase 4: User Story 2 - Execute Safe Incremental Refactor Backlog (Priority: P2)

**Goal**: Burn down critical smells with behavior-preserving refactors in small waves, prioritizing transaction-critical and replay-sensitive modules first.

**Independent Test**: Each wave is mergeable on its own: tests pass, replay/concurrency checks pass where required, and no new critical findings are introduced.

### Wave W1: Ledger Foundation Long Methods (no transaction boundary changes)

- [x] T022 [P] [US2] Add characterization tests around posting outcomes and audit trace stability in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/PostLedgerTransactionCharacterizationTest.java
- [x] T023 [US2] Refactor long method complexity in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/PostLedgerTransactionService.java by extracting pure helpers (no @Transactional changes)
- [x] T024 [US2] Ensure no behavior change by running focused tests: ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.PostLedgerTransactionIntegrationTest
- [x] T025 [US2] Update specs/003-tech-debt-compliance/contracts/refactor-backlog.md marking completed items (e.g., LEDGER-TX-001) and recording evidence (test class + commit)
- W1 execution evidence: focused validation command completed with `BUILD SUCCESSFUL` on 2026-05-19 (after sandbox rerun with elevated permissions).

### Wave W2: Ledger Foundation Duplication (no transaction boundary changes)

- [x] T026 [US2] Add characterization tests for balance query null/scale handling in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/application/query/GetAccountBalanceServiceCharacterizationTest.java
- [x] T027 [US2] Remove duplication in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/query/GetAccountBalanceService.java via extracted pure helpers (preserve money scale rules)
- [x] T028 [US2] Run focused tests for query behavior and any impacted adapters in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/ (add tests if gaps are discovered)
- [x] T029 [US2] Update specs/003-tech-debt-compliance/contracts/refactor-backlog.md marking completed items (e.g., LEDGER-QRY-001) and recording evidence
- W2 execution evidence: focused validation command completed with `BUILD SUCCESSFUL` on 2026-05-19 (after sandbox rerun with elevated permissions).

### Wave W3: Balance Long Methods (no concurrency/replay changes)

- [x] T030 [P] [US2] Add characterization tests for balance mutation outcomes in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceMutationCharacterizationTest.java
- [x] T031 [US2] Refactor long methods in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceRebuildService.java and/or ReserveFundsService.java by extracting pure helpers (no retry/replay ordering changes)
- [x] T032 [US2] Run balance integration suite: ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReplayRebuildIntegrationTest --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationIntegrationTest
- [x] T033 [US2] Run concurrency characterization: ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationConcurrencyIntegrationTest
- [x] T034 [US2] Update specs/003-tech-debt-compliance/contracts/refactor-backlog.md with completed W3 items + evidence
- T032 execution evidence: `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReplayRebuildIntegrationTest --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationIntegrationTest` -> `BUILD SUCCESSFUL` on 2026-05-19 (after sandbox rerun with elevated permissions).
- T033 execution evidence: `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationConcurrencyIntegrationTest` -> `BUILD SUCCESSFUL` on 2026-05-19 (after sandbox rerun with elevated permissions).
- W3 execution evidence: focused validation command completed with `BUILD SUCCESSFUL` on 2026-05-19 (after sandbox rerun with elevated permissions).

### Wave W4: Replay Determinism Hardening (touches: replay only)

- [x] T035 [US2] Audit and harden deterministic ordering boundaries in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceReplayOrderingService.java (explicit comparators only; no transaction changes)
- [x] T036 [US2] Run replay determinism tests: ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReplayDeterminismIntegrationTest
- [x] T037 [US2] Update specs/003-tech-debt-compliance/contracts/refactor-backlog.md with completed BAL-REPLAY-001 evidence and any new determinism findings
- T036 execution evidence: sandbox run failed with `Could not determine a usable wildcard IP for this machine`; elevated rerun succeeded with `BUILD SUCCESSFUL` on 2026-05-19.

### Wave W5: Concurrency Safety (touches: concurrency only)

- [x] T038 [US2] Triage concurrency-related findings into specs/003-tech-debt-compliance/contracts/refactor-backlog.md (lock ordering, retries, backoff, transaction isolation)
- [x] T039 [US2] Refactor concurrency paths (only if needed by findings) in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/ProtectedWriteRetryExecutor.java preserving bounded retries and fail-closed behavior
- [x] T040 [US2] Run concurrency validations: ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationConcurrencyIntegrationTest
- [x] T041 [US2] Run regression/integration tests covering protected writes (add/extend tests under src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/ as needed)
- [x] T042 [US2] Update specs/003-tech-debt-compliance/contracts/refactor-backlog.md with completed BAL-CONC-001 evidence
- T039 triage decision: no code changes required in `ProtectedWriteRetryExecutor`; bounded retry and fail-closed behavior already conform to BAL-CONC-001 constraints.
- T040 execution evidence: sandbox run failed with `Could not determine a usable wildcard IP for this machine`; elevated rerun succeeded with `BUILD SUCCESSFUL` on 2026-05-19.
- T041 execution evidence: sandbox run failed with `Could not determine a usable wildcard IP for this machine`; elevated rerun (`BalanceReservationIntegrationTest` + `BalanceContentionRetryIntegrationTest`) succeeded with `BUILD SUCCESSFUL` on 2026-05-19.

### Wave W6: Transaction Boundary Refactors (only if required)

- [x] T043 [US2] Identify any remaining findings that require transaction boundary changes and record them as dedicated items in specs/003-tech-debt-compliance/contracts/refactor-backlog.md (touches: transaction_boundary)
- [x] T044 [US2] Implement minimal transaction boundary change (single service) using explicit @Transactional scoping in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/** (never mix with replay/concurrency changes)
- [x] T045 [US2] Add/extend transaction-boundary integration tests in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/ validating rollback/commit behavior for the changed service
- [x] T046 [US2] Run replay + concurrency validations where applicable (BalanceReplayDeterminismIntegrationTest + BalanceReservationConcurrencyIntegrationTest)
- [x] T047 [US2] Update specs/003-tech-debt-compliance/contracts/refactor-backlog.md with completed W6 evidence and rollback notes
- T043-T047 decision evidence: W6 marked `DONE (NO-OP)` in `contracts/refactor-backlog.md` because Sonar-only backlog contains only `SONAR-CRIT-20260519-001/002` (`java:S1192`, `touches: none`) and no `touches: transaction_boundary` finding/item on 2026-05-19.

---

## Phase 5: User Story 3 - Protect Ledger Correctness During Refactor (Priority: P3)

**Goal**: Every wave preserves replay determinism, transaction integrity, and balance correctness with mandatory validation for transaction-critical and replay-sensitive modules.

**Independent Test**: Baseline and post-change runs produce identical replay outcomes and pass concurrency and integration checks.

- [x] T048 [P] [US3] Add a “wave validation checklist” section to specs/003-tech-debt-compliance/contracts/quality-gate-policy.md (required tests + evidence links per wave)
- [x] T049 [US3] Add an invariant-focused regression test suite for double-entry and immutability checks in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/domain/DoubleEntryInvariantsTest.java
- [x] T050 [US3] Add balance correctness regression tests for reservation lifecycle invariants in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/BalanceCorrectnessInvariantsTest.java
- [x] T051 [US3] Add a deterministic “replay baseline vs refactor” comparison helper in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/support/ReplayStateAssertions.java and use it from BalanceReplayDeterminismIntegrationTest
- [x] T052 [US3] Document the mandatory validation matrix (module criticality -> required tests) in specs/003-tech-debt-compliance/contracts/risk-matrix.md
- T049-T051 validation command (sandbox): `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.domain.DoubleEntryInvariantsTest --tests com.bangnk.ledgercore.ledger_core.ledger.balance.domain.BalanceCorrectnessInvariantsTest --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReplayDeterminismIntegrationTest --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationConcurrencyIntegrationTest`
- T049-T051 validation result (sandbox): `FAILURE` with `Could not determine a usable wildcard IP for this machine` on 2026-05-19.
- T049-T051 elevated rerun: `BLOCKED` in current session due escalation approval limit; no sandbox-safe alternative can validate Testcontainers-backed replay/concurrency tests.

**Checkpoint**: Correctness validation is explicit, repeatable, and required by policy for transaction-critical work.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Cleanup and consistency improvements that affect multiple stories without changing behavior.

- [x] T053 [P] Align naming and structure across docs in specs/003-tech-debt-compliance/contracts/ (consistent wave IDs, backlog IDs, evidence fields)
- [ ] T054 Tighten Qodana failure conditions gradually (high/moderate thresholds) in qodana.yaml after critical issues are burned down (DONE_WITH_CONCERNS: thresholds active; baseline verification remains blocked by T007 abort)
- [ ] T055 Tighten Sonar quality gate thresholds (if configured on Sonar side) and ensure PR new-code settings align with policy (document in specs/003-tech-debt-compliance/contracts/quality-gate-policy.md) (ABORTED by user on 2026-05-24)
  - Status: Aborted per user instruction; external Sonar server-side verification remains out of scope.
- [x] T056 Run full verification: GRADLE_USER_HOME=/tmp/gradle-home ./gradlew build (plus targeted sonar/qodana runs if tokens available) and update specs/003-tech-debt-compliance/quickstart.md with final commands
  - Evidence (2026-05-24): sandbox run failed with `Could not determine a usable wildcard IP for this machine`; elevated rerun succeeded with `BUILD SUCCESSFUL`.
  - Durable artifact: `specs/003-tech-debt-compliance/contracts/evidence/t056-build-2026-05-24.log` (+ metadata in `t056-build-2026-05-24.meta`).
  - Targeted Sonar/Qodana runs were not executed in this session due unavailable local Sonar auth context and no local Qodana snapshot artifact.
- [x] T057 Record ADR(s) if any wave required architectural deviation or new dependency under docs/ADR/

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 0 (Worktree/Harness) -> Phase 1 (Setup) -> Phase 2 (Foundational) -> Phase 3 (US1/W0) -> Phase 4 (US2/W1-W6) -> Phase 5 (US3) -> Phase 6 (Polish)

### User Story Dependencies

- **US1 (P1)** depends on Phase 2 completing backlog triage scaffolding (at minimum T006-T009).
- **US2 (P2)** depends on US1 (quality gates stable) plus the Phase 2 correctness harness (T010-T013) for transaction-critical modules.
- **US3 (P3)** can be implemented incrementally alongside US2, but its policy and invariant tests should land before higher-risk waves (W4/W5/W6).

### Parallel Opportunities

- [P] tasks in Phase 1 can be done independently (Sonar docs, Qodana baseline docs, standards doc).
- Characterization tests for different modules can be written in parallel (ledger vs balance).
- Backlog triage (Sonar vs Qodana) can be parallelized as long as it converges into specs/003-tech-debt-compliance/contracts/refactor-backlog.md.

---

## Parallel Example: User Story 2 (W1/W2)

```text
Task: "Add characterization tests for posting outcomes in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/PostLedgerTransactionCharacterizationTest.java"
Task: "Refactor PostLedgerTransactionService in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/PostLedgerTransactionService.java"

Task: "Add characterization tests for GetAccountBalanceService in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/application/query/GetAccountBalanceServiceCharacterizationTest.java"
Task: "Refactor duplication in GetAccountBalanceService in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/query/GetAccountBalanceService.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 0-2 (harness + triage)
2. Complete Phase 3 (W0 CI stabilization)
3. STOP and validate CI behavior on a PR into `develop`

### Incremental Delivery

- Deliver one wave at a time (W1..W6), never mixing transaction-boundary changes with replay or concurrency work.
- For every wave touching transaction-critical or replay-sensitive code: require replay + concurrency validations (per policy) before merge.
