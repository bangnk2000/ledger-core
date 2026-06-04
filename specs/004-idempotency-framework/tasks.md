#
# Tasks: Idempotency Framework

**Input**: Design documents from `/specs/004-idempotency-framework/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Integration tests are REQUIRED for every critical business flow. Contract, unit, and additional integration tests MUST be included to validate canonical fingerprinting, duplicate suppression, lifecycle behavior, replay compatibility, retention handling, and concurrent execution safety.

**Execution Workflow**: Every generated task list MUST be executed using the mandatory workflow order from the constitution: worktree -> TDD (red-green-refactor) -> subagent-driven execution -> code review -> finish-branch.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`[US1]`, `[US2]`, `[US3]`)
- Include exact file paths in descriptions

## Phase 0: Worktree and TDD Harness

**Purpose**: Establish isolated execution and test-first discipline before any implementation work starts

- [ ] T000 Create an isolated worktree or equivalent isolated workspace for feature branch `004-idempotency-framework`
- [ ] T001 Capture red-green-refactor and final verification commands in `specs/004-idempotency-framework/quickstart.md`
- [ ] T002 Plan task decomposition and ownership for subagent-driven execution in `specs/004-idempotency-framework/tasks.md`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create the package skeleton and test locations used by all stories

- [ ] T003 Create the `idempotency` module package structure with `package-info.java` files under `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/in/`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/port/in/`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/port/out/`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/config/`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/model/`, and `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/valueobject/`
- [ ] T004 Create the `idempotency` test package structure with `package-info.java` files under `src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/`, `src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/application/`, and `src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/`
- [ ] T005 [P] Add focused verification command examples for the new module to `specs/004-idempotency-framework/quickstart.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T006 Add additive Flyway schema for generic idempotency records, replay windows, tombstones, and lifecycle events in `src/main/resources/db/migration/V3__create_idempotency_framework.sql`
- [ ] T007 [P] Create core value objects for scope, key, fingerprint, retention profile, and claim owner in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/valueobject/IdempotencyScope.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/valueobject/IdempotencyKey.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/valueobject/RequestFingerprint.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/valueobject/RetentionPolicyProfile.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/valueobject/ClaimOwner.java`
- [ ] T008 [P] Create domain models and enums for records, replay outcomes, lifecycle events, and retention status in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/model/IdempotencyRecord.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/model/ReplayOutcome.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/model/LifecycleEvent.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/model/IdempotencyEnums.java`
- [ ] T009 [P] Define inbound use-case contracts for claim, finalize, inspect, and cleanup operations in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/port/in/IdempotencyClaimUseCase.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/port/in/IdempotencyFinalizeUseCase.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/port/in/IdempotencyInspectionUseCase.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/port/in/IdempotencyCleanupUseCase.java`
- [ ] T010 [P] Define outbound persistence and clock/observability ports in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/port/out/IdempotencyRecordRepositoryPort.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/port/out/LifecycleEventRepositoryPort.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/port/out/ReplayOutcomeRepositoryPort.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/port/out/IdempotencyObservabilityPort.java`
- [ ] T011 Create shared application result and error contracts in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyDecision.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyApplicationErrors.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyDecisionTypes.java`
- [ ] T012 Create Spring wiring for the new module in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/config/IdempotencyModuleConfiguration.java`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Execute an external operation safely once (Priority: P1) 🎯 MVP

**Goal**: Allow one protected request to claim execution exactly once, persist a durable record, and replay the original outcome for identical retries without re-running side effects.

**Independent Test**: Submit the same protected request twice with the same scope, key, and canonical fingerprint; verify that only the first attempt is allowed to execute and the second returns the stored outcome.

### Tests for User Story 1 ⚠️

- [ ] T013 [P] [US1] Add domain tests for first-execution, replay, and terminal-state invariants in `src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/IdempotencyRecordTest.java`
- [ ] T014 [P] [US1] Add application tests for successful claim, finalize, and replay resolution in `src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyClaimServiceTest.java`
- [ ] T015 [P] [US1] Add PostgreSQL integration test for record creation and replay outcome persistence in `src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/IdempotencyPersistenceIntegrationTest.java`

### Implementation for User Story 1

- [ ] T016 [US1] Implement first-execution and replay behavior on `IdempotencyRecord` in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/model/IdempotencyRecord.java`
- [ ] T017 [US1] Implement claim and finalize application services in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyClaimService.java` and `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyFinalizeService.java`
- [ ] T018 [US1] Implement inspection query service for completed outcomes in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyInspectionService.java`
- [ ] T019 [US1] Implement JPA entities and repositories for records, replay outcomes, and lifecycle events in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/IdempotencyJpaEntities.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/IdempotencyRecordJpaRepository.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/ReplayOutcomeJpaRepository.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/LifecycleEventJpaRepository.java`
- [ ] T020 [US1] Implement PostgreSQL-backed persistence adapters for claim/finalize/replay flows in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/JpaIdempotencyRecordRepositoryAdapter.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/JpaReplayOutcomeRepositoryAdapter.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/JpaLifecycleEventRepositoryAdapter.java`
- [ ] T021 [US1] Add structured logs and metrics for first execution and replay outcomes in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyObservability.java`

**Checkpoint**: User Story 1 should replay identical completed requests without creating additional side effects

---

## Phase 4: User Story 2 - Detect conflicting reuse and concurrent duplicates (Priority: P2)

**Goal**: Detect conflicting key reuse, enforce canonical fingerprint rules, and ensure only one concurrent caller wins the active claim for a protected request.

**Independent Test**: Submit materially different requests with the same scope and key to verify conflict handling, then submit many concurrent identical requests and verify one active execution with stable duplicate-in-progress outcomes for the rest.

### Tests for User Story 2 ⚠️

- [ ] T022 [P] [US2] Add domain tests for canonical fingerprint comparison and conflicting reuse in `src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/RequestFingerprintTest.java`
- [ ] T023 [P] [US2] Add application tests for duplicate-in-progress and conflict decisions in `src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyConflictServiceTest.java`
- [ ] T024 [P] [US2] Add PostgreSQL concurrency integration test for single-winner claim semantics in `src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/IdempotencyConcurrencyIntegrationTest.java`
- [ ] T025 [P] [US2] Add characterization tests for canonical request-intent translation in `src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/IdempotencyFrameworkLedgerCharacterizationTest.java` and `src/test/java/com/bangnk/ledgercore/ledger_core/balance/adapter/IdempotencyFrameworkBalanceCharacterizationTest.java`

### Implementation for User Story 2

- [ ] T026 [US2] Implement canonical fingerprint normalization rules in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/CanonicalRequestFingerprintFactory.java`
- [ ] T027 [US2] Implement conflict detection and duplicate-in-progress decision logic in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyConflictService.java`
- [ ] T028 [US2] Add atomic single-winner claim queries and locking semantics to `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/JpaIdempotencyRecordRepositoryAdapter.java`
- [ ] T029 [US2] Implement consumer onboarding translators for ledger and balance canonical request intent in `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/LedgerIdempotencyTranslator.java` and `src/main/java/com/bangnk/ledgercore/ledger_core/balance/application/BalanceIdempotencyTranslator.java`
- [ ] T030 [US2] Add duplicate-in-progress and conflict observability signals in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyObservability.java`

**Checkpoint**: User Story 2 should reject conflicting reuse and serialize concurrent identical claims to one active execution

---

## Phase 5: User Story 3 - Manage retention and expiration consistently (Priority: P3)

**Goal**: Apply replay-window and tombstone retention behavior consistently, support cleanup, and preserve safe handling for indeterminate or late retries.

**Independent Test**: Advance stored records through replayable, tombstoned, and purge-eligible states; verify replay behavior changes as documented while re-execution remains blocked until tombstone expiry.

### Tests for User Story 3 ⚠️

- [ ] T031 [P] [US3] Add domain tests for retention-state transitions and tombstone rules in `src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/RetentionPolicyProfileTest.java`
- [ ] T032 [P] [US3] Add application tests for expiration, tombstoning, and indeterminate cleanup behavior in `src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyCleanupServiceTest.java`
- [ ] T033 [P] [US3] Add PostgreSQL integration test for replay-window, tombstone, and purge-eligibility handling in `src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/IdempotencyExpirationIntegrationTest.java`

### Implementation for User Story 3

- [ ] T034 [US3] Implement retention policy and retention-status behavior in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/valueobject/RetentionPolicyProfile.java` and `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/model/IdempotencyRecord.java`
- [ ] T035 [US3] Implement cleanup and expiration application service in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyCleanupService.java`
- [ ] T036 [US3] Add replay-window, tombstone, and purge queries to `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/JpaIdempotencyRecordRepositoryAdapter.java`
- [ ] T037 [US3] Record replay-window and tombstone lifecycle events in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/JpaLifecycleEventRepositoryAdapter.java`
- [ ] T038 [US3] Add expiration and cleanup observability in `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyObservability.java`

**Checkpoint**: User Story 3 should retain late-retry safety through replay and tombstone windows without requiring indefinite full-outcome retention

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Complete onboarding documentation, validation, and release readiness across all stories

- [ ] T039 [P] Update module documentation and execution notes in `specs/004-idempotency-framework/plan.md`, `specs/004-idempotency-framework/research.md`, `specs/004-idempotency-framework/data-model.md`, and `specs/004-idempotency-framework/quickstart.md`
- [ ] T040 Validate zero-downtime migration and coexistence sequencing for `src/main/resources/db/migration/V3__create_idempotency_framework.sql` and `specs/004-idempotency-framework/contracts/consumer-onboarding-contract.md`
- [ ] T041 [P] Add any remaining adapter-level regression coverage for replay compatibility in `src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/` and `src/test/java/com/bangnk/ledgercore/ledger_core/balance/adapter/`
- [ ] T042 Run the full verification suite from `specs/004-idempotency-framework/quickstart.md` and record results in `specs/004-idempotency-framework/quickstart.md`
- [ ] T043 Run code review, resolve or record findings, and capture the finish-branch decision in `specs/004-idempotency-framework/tasks.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 0**: Starts immediately and establishes worktree/TDD discipline for all later work
- **Phase 1**: Depends on Phase 0 and creates the shared source/test structure
- **Phase 2**: Depends on Phase 1 and blocks all user story work
- **Phase 3 (US1)**: Depends on Phase 2 and provides the MVP reusable claim/finalize/replay flow
- **Phase 4 (US2)**: Depends on Phase 3 because conflict and concurrency handling build on the core claim/finalize model
- **Phase 5 (US3)**: Depends on Phase 3 and can proceed after the core lifecycle exists; it should follow Phase 4 if shared persistence changes overlap
- **Phase 6**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Starts after foundational work and delivers the MVP
- **User Story 2 (P2)**: Depends on User Story 1 core claim/finalize infrastructure
- **User Story 3 (P3)**: Depends on User Story 1 lifecycle persistence and shares persistence changes with User Story 2

### Within Each User Story

- Tests MUST be written and observed failing before implementation
- Domain and value objects before application services
- Application services before persistence adapter wiring
- Persistence behavior before consumer-compatibility validation
- Observability and compatibility checks before considering the story complete

### Parallel Opportunities

- Phase 1 tasks marked `[P]` can run in parallel
- In Phase 2, domain/value objects and port definitions marked `[P]` can run in parallel
- In each user story, test tasks marked `[P]` can run in parallel
- Ledger and balance characterization tests can run in parallel with generic application tests once the core contracts exist

---

## Parallel Example: User Story 1

```bash
# Launch US1 tests together:
Task: "T013 [P] [US1] Add domain tests for first-execution, replay, and terminal-state invariants in src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/IdempotencyRecordTest.java"
Task: "T014 [P] [US1] Add application tests for successful claim, finalize, and replay resolution in src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyClaimServiceTest.java"
Task: "T015 [P] [US1] Add PostgreSQL integration test for record creation and replay outcome persistence in src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/IdempotencyPersistenceIntegrationTest.java"

# Launch US1 persistence files together after tests are red:
Task: "T019 [US1] Implement JPA entities and repositories for records, replay outcomes, and lifecycle events in src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/IdempotencyJpaEntities.java, src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/IdempotencyRecordJpaRepository.java, src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/ReplayOutcomeJpaRepository.java, and src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/LifecycleEventJpaRepository.java"
Task: "T020 [US1] Implement PostgreSQL-backed persistence adapters for claim/finalize/replay flows in src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/JpaIdempotencyRecordRepositoryAdapter.java, src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/JpaReplayOutcomeRepositoryAdapter.java, and src/main/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/out/persistence/JpaLifecycleEventRepositoryAdapter.java"
```

---

## Parallel Example: User Story 2

```bash
# Launch US2 behavior tests together:
Task: "T022 [P] [US2] Add domain tests for canonical fingerprint comparison and conflicting reuse in src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/RequestFingerprintTest.java"
Task: "T023 [P] [US2] Add application tests for duplicate-in-progress and conflict decisions in src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyConflictServiceTest.java"
Task: "T024 [P] [US2] Add PostgreSQL concurrency integration test for single-winner claim semantics in src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/IdempotencyConcurrencyIntegrationTest.java"
Task: "T025 [P] [US2] Add characterization tests for canonical request-intent translation in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/IdempotencyFrameworkLedgerCharacterizationTest.java and src/test/java/com/bangnk/ledgercore/ledger_core/balance/adapter/IdempotencyFrameworkBalanceCharacterizationTest.java"
```

---

## Parallel Example: User Story 3

```bash
# Launch US3 retention tests together:
Task: "T031 [P] [US3] Add domain tests for retention-state transitions and tombstone rules in src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/domain/RetentionPolicyProfileTest.java"
Task: "T032 [P] [US3] Add application tests for expiration, tombstoning, and indeterminate cleanup behavior in src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/application/IdempotencyCleanupServiceTest.java"
Task: "T033 [P] [US3] Add PostgreSQL integration test for replay-window, tombstone, and purge-eligibility handling in src/test/java/com/bangnk/ledgercore/ledger_core/idempotency/adapter/IdempotencyExpirationIntegrationTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 0
2. Complete Phase 1
3. Complete Phase 2
4. Complete Phase 3
5. **STOP and VALIDATE**: verify identical retries replay the original durable outcome without additional side effects

### Incremental Delivery

1. Deliver the generic claim/finalize/replay capability first through User Story 1
2. Add canonical fingerprinting, conflict handling, and concurrent duplicate safety in User Story 2
3. Add replay-window and tombstone retention behavior in User Story 3
4. Finish with coexistence validation, migration review, and full verification in Phase 6
