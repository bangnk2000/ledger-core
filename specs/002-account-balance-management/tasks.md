# Tasks: Account Balance Management

**Input**: Design documents from `/specs/002-account-balance-management/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Required by the feature specification and implementation plan. Contract tests, domain tests, PostgreSQL-backed integration tests, concurrency tests, and deterministic replay tests are included before their related implementation tasks.

**Organization**: Tasks are grouped by user story so each story can be implemented and tested as an independent increment after the shared foundation is complete.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel because it touches different files and has no dependency on incomplete tasks in the same phase
- **[Story]**: User story label for story phases only
- Every task includes an exact file path

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish package boundaries, migration entrypoint, and test support for the balance bounded context.

- [X] T001 Create balance bounded-context package structure under src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/
- [X] T002 Create balance test package structure under src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/
- [X] T003 [P] Add additive balance schema migration skeleton in src/main/resources/db/migration/V2__create_balance_management.sql
- [X] T004 [P] Add PostgreSQL Testcontainers base fixture in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/PostgresIntegrationTestSupport.java
- [X] T005 [P] Add balance API contract fixture helpers in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceApiContractSupport.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core contracts, value objects, persistence tables, and adapter boundaries that all user stories depend on.

**CRITICAL**: No user story work should begin until this phase is complete.

- [X] T006 Define balance domain enums in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/model/BalanceEnums.java
- [X] T007 [P] Implement AccountId value object in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/valueobject/AccountId.java
- [X] T008 [P] Implement CurrencyCode value object in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/valueobject/CurrencyCode.java
- [X] T009 [P] Implement MoneyAmount value object in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/valueobject/MoneyAmount.java
- [X] T010 [P] Implement RequestIdentity value object in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/valueobject/RequestIdentity.java
- [X] T011 [P] Implement ActorContext value object in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/valueobject/ActorContext.java
- [X] T012 Implement balance state, reservation, snapshot, rebuild, checkpoint, reconciliation, and idempotency tables in src/main/resources/db/migration/V2__create_balance_management.sql
- [X] T013 Define immutable ledger replay input port in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/out/LedgerReplayExportPort.java
- [X] T014 Define ledger posting reference port in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/out/LedgerPostingReferencePort.java
- [X] T015 Define balance persistence port in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/out/BalanceStateRepositoryPort.java
- [X] T016 Define reservation persistence port in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/out/FundsReservationRepositoryPort.java
- [X] T017 Define idempotency persistence port in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/out/BalanceIdempotencyRepositoryPort.java
- [X] T018 Define rebuild and reconciliation persistence ports in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/out/BalanceRecoveryRepositoryPort.java
- [X] T019 Define balance transaction boundary port in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/out/BalanceTransactionPort.java
- [X] T020 Add balance exception and outcome types in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceApplicationErrors.java
- [X] T021 Add Spring balance module configuration in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/config/BalanceManagementConfiguration.java
- [X] T022 Add HTTP error mapping for balance outcomes in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceApiExceptionHandler.java

**Checkpoint**: Foundation ready. User story implementation can begin in priority order or in parallel by story.

---

## Phase 3: User Story 1 - Authorize Spendable Funds Safely (Priority: P1) MVP

**Goal**: Confirm available funds and create a reservation in one protected balance operation so concurrent spend requests cannot overspend.

**Independent Test**: Submit at least 100 concurrent debit or transfer reservation attempts against overlapping accounts and verify that successful reservations never exceed available funds while rejected attempts return stable insufficient-funds or contention outcomes.

### Tests for User Story 1

- [ ] T023 [P] [US1] Add domain tests for available-balance calculation and negative-balance prevention in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/BalanceStateTest.java
- [ ] T024 [P] [US1] Add domain tests for active reservation creation and idempotent intent identity in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/FundsReservationTest.java
- [ ] T025 [P] [US1] Add reserve funds HTTP contract test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/ReserveFundsContractTest.java
- [ ] T026 [P] [US1] Add reservation integration test for successful reserve and insufficient funds in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReservationIntegrationTest.java
- [ ] T027 [P] [US1] Add concurrent reservation integration test for overlapping debits in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReservationConcurrencyIntegrationTest.java

### Implementation for User Story 1

- [ ] T028 [P] [US1] Implement BalanceState aggregate in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/model/BalanceState.java
- [ ] T029 [P] [US1] Implement FundsReservation aggregate in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/model/FundsReservation.java
- [ ] T030 [P] [US1] Implement BalanceMutationRequest command model in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/command/BalanceMutationRequest.java
- [ ] T031 [US1] Define reserve funds use case in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/in/ReserveFundsUseCase.java
- [ ] T032 [US1] Implement reserve funds application service in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/ReserveFundsService.java
- [ ] T033 [US1] Implement JPA balance state entity and mapper in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/out/persistence/BalanceStateJpaEntity.java
- [ ] T034 [US1] Implement JPA reservation entity and mapper in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/out/persistence/FundsReservationJpaEntity.java
- [ ] T035 [US1] Implement balance state repository adapter with deterministic locking in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/out/persistence/JpaBalanceStateRepositoryAdapter.java
- [ ] T036 [US1] Implement reservation repository adapter in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/out/persistence/JpaFundsReservationRepositoryAdapter.java
- [ ] T037 [US1] Implement idempotency repository adapter in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/out/persistence/JpaBalanceIdempotencyRepositoryAdapter.java
- [ ] T038 [US1] Implement Spring transaction adapter for protected writes in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/out/persistence/SpringBalanceTransactionAdapter.java
- [ ] T039 [US1] Implement reserve funds controller in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/ReservationController.java
- [ ] T040 [US1] Implement reserve request and outcome DTOs in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/ReservationDtos.java
- [ ] T041 [US1] Add reserve operation metrics and structured logs in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceObservability.java

**Checkpoint**: User Story 1 is independently functional and testable as the MVP.

---

## Phase 4: User Story 2 - Query Reliable Current Balances (Priority: P1)

**Goal**: Return current ledger, locked, pending, available, version, and freshness metadata for a single account balance view.

**Independent Test**: Seed posted ledger activity and reservations, request the current balance snapshot, and verify every component matches the documented formula and as-of metadata.

### Tests for User Story 2

- [ ] T042 [P] [US2] Add current balance HTTP contract test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/GetCurrentBalanceContractTest.java
- [ ] T043 [P] [US2] Add current balance integration test for zero and populated balances in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceSnapshotIntegrationTest.java
- [ ] T044 [P] [US2] Add snapshot derivation domain tests in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/BalanceSnapshotTest.java

### Implementation for User Story 2

- [ ] T045 [P] [US2] Implement BalanceSnapshot model in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/model/BalanceSnapshot.java
- [ ] T046 [US2] Define get current balance use case in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/in/GetCurrentBalanceUseCase.java
- [ ] T047 [US2] Implement current balance query service in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/query/GetCurrentBalanceQueryService.java
- [ ] T048 [US2] Implement balance snapshot repository adapter in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/out/persistence/JpaBalanceSnapshotRepositoryAdapter.java
- [ ] T049 [US2] Implement current balance controller in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceQueryController.java
- [ ] T050 [US2] Implement current balance DTOs in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceQueryDtos.java
- [ ] T051 [US2] Add balance read freshness metrics and structured logs in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceObservability.java

**Checkpoint**: User Stories 1 and 2 provide protected reservation writes and reliable current balance reads.

---

## Phase 5: User Story 3 - Finalize or Release Pending Funds Deterministically (Priority: P2)

**Goal**: Move reservations through confirm, expire, cancel, and rollback flows exactly once without orphaned or double-counted funds.

**Independent Test**: Create reservations, confirm some into ledger posting references, cancel or expire others, and verify reservation states and balance components transition exactly once under retries.

### Tests for User Story 3

- [ ] T052 [P] [US3] Add confirm reservation HTTP contract test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/ConfirmReservationContractTest.java
- [ ] T053 [P] [US3] Add release reservation HTTP contract test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/ReleaseReservationContractTest.java
- [ ] T054 [P] [US3] Add reservation lifecycle integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReservationLifecycleIntegrationTest.java
- [ ] T055 [P] [US3] Add reservation recovery integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReservationRecoveryIntegrationTest.java

### Implementation for User Story 3

- [ ] T056 [US3] Define confirm reservation use case in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/in/ConfirmReservationUseCase.java
- [ ] T057 [US3] Define release reservation use case in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/in/ReleaseReservationUseCase.java
- [ ] T058 [US3] Implement reservation lifecycle service in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/ReservationLifecycleService.java
- [ ] T059 [US3] Implement ledger posting reference adapter stub boundary in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/out/persistence/ContractLedgerPostingReferenceAdapter.java
- [ ] T060 [US3] Extend reservation controller with confirm and release endpoints in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/ReservationController.java
- [ ] T061 [US3] Extend reservation DTOs for confirm and release requests in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/ReservationDtos.java
- [ ] T062 [US3] Add expiration processing service in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/ReservationExpirationService.java
- [ ] T063 [US3] Add lifecycle metrics and structured logs in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceObservability.java

**Checkpoint**: Reservation creation, confirmation, release, cancellation, expiration, and recovery are independently testable.

---

## Phase 6: User Story 4 - Rebuild and Reconcile Balances From History (Priority: P2)

**Goal**: Rebuild derived balance state from immutable history and reservation history, then record reconciliation discrepancies without changing ledger history.

**Independent Test**: Seed replay-safe ledger and reservation history, run rebuilds in deterministic order, rerun from checkpoints, and verify that matching state passes while injected drift produces traceable reconciliation records.

### Tests for User Story 4

- [ ] T064 [P] [US4] Add rebuild job HTTP contract test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceRebuildContractTest.java
- [ ] T065 [P] [US4] Add reconciliation HTTP contract test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceReconciliationContractTest.java
- [ ] T066 [P] [US4] Add deterministic replay integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReplayRebuildIntegrationTest.java
- [ ] T067 [P] [US4] Add reconciliation drift integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReconciliationIntegrationTest.java

### Implementation for User Story 4

- [ ] T068 [P] [US4] Implement BalanceRebuildJob model in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/model/BalanceRebuildJob.java
- [ ] T069 [P] [US4] Implement BalanceRebuildCheckpoint model in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/model/BalanceRebuildCheckpoint.java
- [ ] T070 [P] [US4] Implement BalanceReconciliationRecord model in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/model/BalanceReconciliationRecord.java
- [ ] T071 [US4] Define rebuild use cases in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/in/BalanceRebuildUseCase.java
- [ ] T072 [US4] Define reconciliation use case in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/in/BalanceReconciliationUseCase.java
- [ ] T073 [US4] Implement replay ordering service in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceReplayOrderingService.java
- [ ] T074 [US4] Implement rebuild application service in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceRebuildService.java
- [ ] T075 [US4] Implement reconciliation application service in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceReconciliationService.java
- [ ] T076 [US4] Implement recovery repository adapter in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/out/persistence/JpaBalanceRecoveryRepositoryAdapter.java
- [ ] T077 [US4] Implement ledger replay export adapter boundary in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/out/persistence/ContractLedgerReplayExportAdapter.java
- [ ] T078 [US4] Implement rebuild controller in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceRebuildController.java
- [ ] T079 [US4] Implement reconciliation controller in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceReconciliationController.java
- [ ] T080 [US4] Implement rebuild and reconciliation DTOs in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceRecoveryDtos.java
- [ ] T081 [US4] Add rebuild progress and reconciliation discrepancy metrics in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceObservability.java

**Checkpoint**: Rebuild and reconciliation can run independently from immutable history and report drift.

---

## Phase 7: User Story 5 - Survive High-Contention and Failure Conditions (Priority: P3)

**Goal**: Make balance write flows predictable under lock timeouts, deadlocks, retries, crashes, duplicate requests, and degraded balance-management state.

**Independent Test**: Induce lock contention, duplicate requests, retry windows, and recovery states, then verify stable idempotent outcomes and no duplicate or orphaned balance mutations.

### Tests for User Story 5

- [ ] T082 [P] [US5] Add retry and lock-timeout integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceContentionRetryIntegrationTest.java
- [ ] T083 [P] [US5] Add duplicate request and idempotency conflict integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceIdempotencyIntegrationTest.java
- [ ] T084 [P] [US5] Add degraded state fail-closed integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceFailClosedIntegrationTest.java

### Implementation for User Story 5

- [ ] T085 [US5] Implement retry policy model in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceRetryPolicy.java
- [ ] T086 [US5] Implement protected write retry executor in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/ProtectedWriteRetryExecutor.java
- [ ] T087 [US5] Implement idempotency outcome conflict detection in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceIdempotencyService.java
- [ ] T088 [US5] Implement degraded-state guard for protected writes in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceConsistencyGuard.java
- [ ] T089 [US5] Add contention, retry, duplicate, and fail-closed observability in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceObservability.java

**Checkpoint**: Protected writes fail closed or retry idempotently under contention and recovery conditions.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Finish verification, rollout documentation, compatibility checks, and operational hardening across all stories.

- [ ] T090 [P] Document explicit transaction boundaries and rollback behavior in docs/account-balance-management-transaction-boundaries.md
- [ ] T091 [P] Document additive API compatibility and replay export compatibility in docs/account-balance-management-compatibility.md
- [ ] T092 [P] Document zero-downtime migration and roll-forward recovery steps in docs/account-balance-management-rollout.md
- [ ] T093 [P] Review ADR assumptions and add account balance management ADR notes in docs/ADR/ADR-009-account-balance-management.md
- [ ] T094 Verify all balance API contract tests under src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/ with GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web.*ContractTest
- [ ] T095 Verify all balance domain tests under src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/ with GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.domain.*
- [ ] T096 Verify all PostgreSQL-backed balance integration tests under src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/ with GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.*
- [ ] T097 Run full regression suite covering src/test/java/com/bangnk/ledgercore/ledger_core/ with GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test
- [ ] T098 Validate quickstart smoke commands in specs/002-account-balance-management/quickstart.md
- [ ] T099 Review generated implementation against .specify/memory/constitution.md and docs/ADR/ for immutable ledger, double-entry, transaction, and modular-monolith compliance

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; can start immediately.
- **Foundational (Phase 2)**: Depends on Setup and blocks every user story.
- **User Story 1 (Phase 3)**: Depends on Foundational and is the MVP.
- **User Story 2 (Phase 4)**: Depends on Foundational; can proceed alongside US1 after shared model and repository decisions are stable.
- **User Story 3 (Phase 5)**: Depends on US1 because it finalizes and releases reservations created by US1.
- **User Story 4 (Phase 6)**: Depends on Foundational and benefits from US1-US3 history, but rebuild and reconciliation can be implemented against fixtures independently.
- **User Story 5 (Phase 7)**: Depends on US1 and US3 write flows because it hardens their retry, duplicate, and fail-closed behavior.
- **Polish (Phase 8)**: Depends on whichever user stories are included in the release scope.

### User Story Dependencies

- **US1 Authorize Spendable Funds Safely (P1)**: First MVP after Foundational; no story dependency.
- **US2 Query Reliable Current Balances (P1)**: No story dependency after Foundational; integrates with US1 state when both exist.
- **US3 Finalize or Release Pending Funds Deterministically (P2)**: Depends on US1 reservation creation.
- **US4 Rebuild and Reconcile Balances From History (P2)**: Can start after Foundational using replay fixtures; complete validation benefits from US1-US3 generated history.
- **US5 Survive High-Contention and Failure Conditions (P3)**: Depends on US1 and US3 write flows.

### Within Each User Story

- Write the story tests first and confirm they fail before implementation.
- Implement domain models before application services.
- Define inbound and outbound ports before adapters.
- Implement persistence adapters before HTTP endpoints that depend on durable state.
- Add observability in the story phase before declaring the story complete.
- Validate each story independently before starting dependent stories.

---

## Parallel Opportunities

- T003, T004, and T005 can run in parallel after package paths are created.
- T007 through T011 can run in parallel because they are independent value objects.
- T013 through T019 can run in parallel after value-object naming is agreed.
- T023 through T027 can run in parallel as independent US1 tests.
- T028 through T030 can run in parallel as independent US1 domain and command classes.
- T042 through T044 can run in parallel as independent US2 tests.
- T052 through T055 can run in parallel as independent US3 tests.
- T064 through T067 can run in parallel as independent US4 tests.
- T068 through T070 can run in parallel as independent US4 domain models.
- T082 through T084 can run in parallel as independent US5 integration tests.
- T090 through T093 can run in parallel as independent documentation and ADR tasks.

---

## Parallel Example: User Story 1

```bash
# Start independent US1 tests together:
Task: "T023 [P] [US1] Add domain tests for available-balance calculation and negative-balance prevention in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/BalanceStateTest.java"
Task: "T024 [P] [US1] Add domain tests for active reservation creation and idempotent intent identity in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/FundsReservationTest.java"
Task: "T025 [P] [US1] Add reserve funds HTTP contract test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/ReserveFundsContractTest.java"
Task: "T026 [P] [US1] Add reservation integration test for successful reserve and insufficient funds in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReservationIntegrationTest.java"
Task: "T027 [P] [US1] Add concurrent reservation integration test for overlapping debits in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReservationConcurrencyIntegrationTest.java"

# Start independent US1 models together:
Task: "T028 [P] [US1] Implement BalanceState aggregate in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/model/BalanceState.java"
Task: "T029 [P] [US1] Implement FundsReservation aggregate in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/model/FundsReservation.java"
Task: "T030 [P] [US1] Implement BalanceMutationRequest command model in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/command/BalanceMutationRequest.java"
```

## Parallel Example: User Story 2

```bash
Task: "T042 [P] [US2] Add current balance HTTP contract test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/GetCurrentBalanceContractTest.java"
Task: "T043 [P] [US2] Add current balance integration test for zero and populated balances in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceSnapshotIntegrationTest.java"
Task: "T044 [P] [US2] Add snapshot derivation domain tests in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/domain/BalanceSnapshotTest.java"
```

## Parallel Example: User Story 3

```bash
Task: "T052 [P] [US3] Add confirm reservation HTTP contract test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/ConfirmReservationContractTest.java"
Task: "T053 [P] [US3] Add release reservation HTTP contract test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/ReleaseReservationContractTest.java"
Task: "T054 [P] [US3] Add reservation lifecycle integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReservationLifecycleIntegrationTest.java"
Task: "T055 [P] [US3] Add reservation recovery integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReservationRecoveryIntegrationTest.java"
```

## Parallel Example: User Story 4

```bash
Task: "T064 [P] [US4] Add rebuild job HTTP contract test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceRebuildContractTest.java"
Task: "T065 [P] [US4] Add reconciliation HTTP contract test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceReconciliationContractTest.java"
Task: "T066 [P] [US4] Add deterministic replay integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReplayRebuildIntegrationTest.java"
Task: "T067 [P] [US4] Add reconciliation drift integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceReconciliationIntegrationTest.java"
```

## Parallel Example: User Story 5

```bash
Task: "T082 [P] [US5] Add retry and lock-timeout integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceContentionRetryIntegrationTest.java"
Task: "T083 [P] [US5] Add duplicate request and idempotency conflict integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceIdempotencyIntegrationTest.java"
Task: "T084 [P] [US5] Add degraded state fail-closed integration test in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceFailClosedIntegrationTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 setup.
2. Complete Phase 2 foundational domain, port, migration, transaction, and adapter boundaries.
3. Complete Phase 3 User Story 1 tests and implementation.
4. Validate US1 independently with the reserve contract test, reservation integration test, and concurrency integration test.
5. Stop and review funds-safety, idempotency, and lock-ordering behavior before implementing dependent lifecycle flows.

### Incremental Delivery

1. Deliver Setup and Foundational infrastructure.
2. Deliver US1 protected reservation writes as MVP.
3. Deliver US2 balance snapshots for read consumers.
4. Deliver US3 lifecycle finalization and release flows.
5. Deliver US4 rebuild and reconciliation recovery tooling.
6. Deliver US5 contention and failure hardening.
7. Finish polish documentation, compatibility review, and full regression verification.

### Parallel Team Strategy

1. Complete Setup and Foundational tasks together.
2. After Foundational completion, split independent streams by user story where capacity allows.
3. Keep US3 and US5 aligned with US1 because they harden and extend protected write flows.
4. Keep US4 replay/reconciliation work isolated behind `LedgerReplayExportPort` so it can progress with replay fixtures while live write flows mature.
