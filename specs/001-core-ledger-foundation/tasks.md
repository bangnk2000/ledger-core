---
description: "Task list for Core Ledger Foundation implementation"
---

# Tasks: Core Ledger Foundation

**Input**: Design documents from `/specs/001-core-ledger-foundation/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Integration tests are required for critical business flows in this feature. Contract and unit tests are included where they validate API compatibility, domain invariants, persistence behavior, and regressions identified by the specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel because it touches different files and does not depend on incomplete tasks in the same phase.
- **[Story]**: User story label for story phases only.
- All task descriptions include exact file paths.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare test and module structure used by all ledger stories.

- [X] T001 Add Testcontainers PostgreSQL test dependencies in build.gradle
- [X] T002 Create integration-test datasource profile in src/test/resources/application-test.yaml
- [X] T003 [P] Create ledger domain package documentation in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/domain/package-info.java
- [X] T004 [P] Create ledger application package documentation in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/package-info.java
- [X] T005 [P] Create ledger web adapter package documentation in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/package-info.java
- [X] T006 [P] Create ledger persistence adapter package documentation in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/out/persistence/package-info.java
- [X] T007 [P] Create shared PostgreSQL integration test base in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/PostgresIntegrationTestBase.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core schema, shared value objects, ports, and error/outcome types that must exist before any user story implementation.

**Critical**: No user story work should begin until this phase is complete.

- [X] T008 Create additive Flyway migration for ledger transactions, ledger entries, idempotency records, constraints, and indexes in src/main/resources/db/migration/V1__create_ledger_foundation.sql
- [X] T009 [P] Create ledger enums Direction, TransactionStatus, ActorType, and PostingOutcomeType in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/domain/valueobject/LedgerEnums.java
- [X] T010 [P] Create Money value object with fixed-precision positive amount validation in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/domain/valueobject/Money.java
- [X] T011 [P] Create AccountId, LedgerTransactionId, LedgerEntryId, and LineId value objects in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/domain/valueobject/LedgerIds.java
- [X] T012 [P] Create RequestIdentity and RequestHash value objects in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/domain/valueobject/RequestIdentity.java
- [X] T013 [P] Create Actor and AuditTrace value objects in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/domain/valueobject/AuditTrace.java
- [X] T014 Create posting outcome and domain exception types in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/PostingOutcome.java
- [X] T015 Create application inbound ports PostLedgerTransactionUseCase and GetAccountBalanceQuery in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/port/in/LedgerUseCases.java
- [X] T016 Create outbound ports LedgerTransactionRepository, LedgerEntryRepository, IdempotencyRecordRepository, and AuditEventPublisher in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/port/out/LedgerPorts.java
- [X] T017 Create Spring transaction boundary configuration for ledger use cases in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/config/LedgerTransactionConfig.java
- [X] T018 Create shared API error response mapper for ledger outcomes in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/LedgerExceptionHandler.java

**Checkpoint**: Foundation ready - user story implementation can now begin.

---

## Phase 3: User Story 1 - Post a Balanced Ledger Transaction (Priority: P1) MVP

**Goal**: Accept balanced debit/credit posting requests and reject invalid or unbalanced requests without visible partial ledger entries.

**Independent Test**: Submit one valid transaction containing at least one debit and one credit and verify acceptance, atomic transaction persistence, all entries recorded together, and debit/credit totals balance. Submit unbalanced and incomplete requests and verify rejection with no entries recorded.

### Tests for User Story 1

- [X] T019 [P] [US1] Add posting API contract tests for 201 accepted, 400 unbalanced, and 400 missing debit or credit in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/PostLedgerTransactionContractTest.java
- [X] T020 [P] [US1] Add domain invariant tests for balanced totals, missing directions, duplicate line IDs, and invalid amounts in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/domain/LedgerTransactionTest.java
- [X] T021 [US1] Add PostgreSQL integration test for atomic accepted posting and rejected posting rollback in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/PostLedgerTransactionIntegrationTest.java

### Implementation for User Story 1

- [X] T022 [P] [US1] Create LedgerEntry domain model with immutable financial fields in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/domain/model/LedgerEntry.java
- [X] T023 [P] [US1] Create LedgerTransaction aggregate with debit/credit balancing and line uniqueness rules in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/domain/model/LedgerTransaction.java
- [X] T024 [US1] Create PostLedgerTransactionCommand and entry command types in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/PostLedgerTransactionCommand.java
- [X] T025 [US1] Implement PostLedgerTransactionService with explicit transaction boundary and no partial-post visibility in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/PostLedgerTransactionService.java
- [X] T026 [US1] Create JPA entities LedgerTransactionJpaEntity and LedgerEntryJpaEntity in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/out/persistence/LedgerJpaEntities.java
- [X] T027 [US1] Implement ledger transaction and entry persistence adapter in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/out/persistence/JpaLedgerPersistenceAdapter.java
- [X] T028 [US1] Create posting request and response DTOs matching the OpenAPI contract in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/PostLedgerTransactionDtos.java
- [X] T029 [US1] Implement POST /api/v1/ledger/postings controller in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/LedgerPostingController.java
- [X] T030 [US1] Wire ledger use cases and persistence adapters in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/config/LedgerModuleConfig.java

**Checkpoint**: User Story 1 is independently testable and provides the MVP ledger posting path.

---

## Phase 4: User Story 2 - Preserve Immutable Ledger History (Priority: P1)

**Goal**: Ensure posted ledger entries cannot be altered or deleted through supported behavior and corrections are represented as new traceable postings.

**Independent Test**: Post a transaction, attempt to update or delete one of its entries through persistence/application behavior, verify the original entry remains unchanged, and verify correction behavior creates a new posting instead of mutating history.

### Tests for User Story 2

- [X] T031 [P] [US2] Add immutability domain tests for blocked mutation of amount, direction, account, transaction reference, and trace details in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/domain/LedgerEntryImmutabilityTest.java
- [X] T032 [P] [US2] Add persistence immutability integration tests for update and delete attempts against posted entries in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/LedgerEntryImmutabilityIntegrationTest.java
- [X] T033 [US2] Add correction-posting integration test that verifies corrections create new entries in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/LedgerCorrectionIntegrationTest.java

### Implementation for User Story 2

- [X] T034 [US2] Harden LedgerEntry domain model mutation surface in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/domain/model/LedgerEntry.java
- [X] T035 [US2] Add correction command factory for reversing and adjusting postings in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/LedgerCorrectionFactory.java
- [X] T036 [US2] Configure JPA entity mapping to prevent application-level updates of posted ledger entry financial columns in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/out/persistence/LedgerJpaEntities.java
- [X] T037 [US2] Add database immutability trigger or guarded persistence rule to Flyway migration in src/main/resources/db/migration/V1__create_ledger_foundation.sql
- [X] T038 [US2] Add retrieval method for original posting details in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/out/persistence/JpaLedgerPersistenceAdapter.java

**Checkpoint**: User Story 2 is independently testable against existing posted entries and correction postings.

---

## Phase 5: User Story 3 - Submit Transactions Safely Under Retry and Concurrency (Priority: P2)

**Goal**: Make mutating posting requests retry-safe and concurrency-safe using durable idempotency records and database constraints.

**Independent Test**: Submit the same request multiple times and verify a stable duplicate outcome without duplicate entries; submit conflicting content with the same request identity and verify conflict; submit at least 100 concurrent valid postings and verify all accepted transactions remain balanced and no partial records are visible.

### Tests for User Story 3

- [X] T039 [P] [US3] Add API contract tests for 200 duplicate and 409 conflicting idempotency outcomes in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/PostLedgerTransactionIdempotencyContractTest.java
- [X] T040 [P] [US3] Add idempotency service tests for request hash matching, request hash conflict, and stable stored outcomes in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/application/IdempotencyServiceTest.java
- [X] T041 [US3] Add PostgreSQL concurrency integration test for 100 simultaneous valid postings in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/LedgerPostingConcurrencyIntegrationTest.java

### Implementation for User Story 3

- [X] T042 [P] [US3] Create IdempotencyRecord domain model in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/domain/model/IdempotencyRecord.java
- [X] T043 [P] [US3] Create canonical request hashing service in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/PostingRequestHasher.java
- [X] T044 [US3] Implement IdempotencyService with duplicate, conflict, accepted, rejected, and failed outcomes in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/IdempotencyService.java
- [X] T045 [US3] Create IdempotencyRecordJpaEntity and repository mapping in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/out/persistence/IdempotencyJpaAdapter.java
- [X] T046 [US3] Integrate idempotency lookup and persistence into posting transaction boundary in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/PostLedgerTransactionService.java
- [X] T047 [US3] Map duplicate and conflict outcomes in posting controller responses in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/LedgerPostingController.java

**Checkpoint**: User Story 3 is independently testable through retry and concurrent posting scenarios.

---

## Phase 6: User Story 4 - Calculate Balances From Ledger Entries (Priority: P2)

**Goal**: Calculate account balances from committed posted ledger entries without mutable balance history.

**Independent Test**: Post several balanced transactions for an account, request its balance, and verify the result equals the net effect of all posted entries. Request balance for an account with no entries and verify zero.

### Tests for User Story 4

- [X] T048 [P] [US4] Add balance API contract tests for existing account and no-entry account responses in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/GetAccountBalanceContractTest.java
- [X] T049 [P] [US4] Add account balance query tests for debit and credit sign interpretation in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/application/GetAccountBalanceServiceTest.java
- [X] T050 [US4] Add PostgreSQL integration test for balance over at least 10000 posted entries within target time in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/GetAccountBalanceIntegrationTest.java

### Implementation for User Story 4

- [X] T051 [P] [US4] Create AccountBalance query model in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/query/AccountBalance.java
- [X] T052 [US4] Implement GetAccountBalanceService using committed posted entries only in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/query/GetAccountBalanceService.java
- [X] T053 [US4] Add balance aggregation query to persistence adapter in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/out/persistence/JpaLedgerPersistenceAdapter.java
- [X] T054 [US4] Create balance response DTO matching the OpenAPI contract in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/GetAccountBalanceDtos.java
- [X] T055 [US4] Implement GET /api/v1/ledger/accounts/{accountId}/balance controller in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/LedgerBalanceController.java

**Checkpoint**: User Story 4 is independently testable through balance queries after postings.

---

## Phase 7: User Story 5 - Trace and Audit Every Posting (Priority: P3)

**Goal**: Retain and expose trace details for accepted and rejected postings while emitting structured audit and operational events.

**Independent Test**: Post a transaction with metadata, actor, request, and correlation details, then verify those details are retrievable with the transaction and entries. Submit a rejected posting and verify structured logs/events include sanitized rejection and trace details.

### Tests for User Story 5

- [X] T056 [P] [US5] Add trace retention integration test for accepted postings in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/LedgerTraceRetentionIntegrationTest.java
- [X] T057 [P] [US5] Add audit event tests for accepted, rejected, duplicate, conflict, failed, and balance-calculated events in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/application/AuditEventPublisherTest.java
- [X] T058 [US5] Add web contract test for sanitized rejected posting outcome with trace details in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/PostLedgerTransactionTraceContractTest.java

### Implementation for User Story 5

- [X] T059 [US5] Persist transaction metadata, actor, correlation, and causation fields in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/out/persistence/LedgerJpaEntities.java
- [X] T060 [US5] Add audit event model and publisher port implementation for structured logs in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/out/persistence/StructuredAuditEventPublisher.java
- [X] T061 [US5] Emit audit events from posting, duplicate, conflict, rejection, failure, and balance use cases in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/PostLedgerTransactionService.java
- [X] T062 [US5] Add Micrometer counters and timers for ledger posting and balance outcomes in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/config/LedgerObservabilityConfig.java
- [X] T063 [US5] Include safe trace fields in posting and balance API responses in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/PostLedgerTransactionDtos.java

**Checkpoint**: User Story 5 is independently testable through trace retrieval and audit/operational event assertions.

---

## Final Phase: Polish & Cross-Cutting Concerns

**Purpose**: Validate compatibility, migration safety, performance, security, and documentation across all stories.

- [X] T064 [P] Document zero-downtime migration and rollback or roll-forward strategy in docs/ai/migration-rules.md
- [X] T065 [P] Document API idempotency, backward compatibility, and error contract decisions in docs/ai/api-guidelines.md
- [X] T066 [P] Document ledger observability signals, metrics, and traces in docs/ai/engineering-rules.md
- [X] T067 Verify quickstart smoke commands and update examples in specs/001-core-ledger-foundation/quickstart.md
- [X] T068 Run full verification and record any remaining test gaps in specs/001-core-ledger-foundation/quickstart.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion; blocks all user story work.
- **User Story 1 (Phase 3)**: Depends on Foundational; MVP posting path.
- **User Story 2 (Phase 4)**: Depends on User Story 1 persisted posting behavior.
- **User Story 3 (Phase 5)**: Depends on User Story 1 posting behavior and Foundational idempotency schema.
- **User Story 4 (Phase 6)**: Depends on User Story 1 posted entries; benefits from User Story 3 concurrency coverage.
- **User Story 5 (Phase 7)**: Depends on posting and balance use cases from User Stories 1, 3, and 4.
- **Polish**: Depends on all selected user stories being complete.

### User Story Dependencies

- **US1 (P1)**: Required MVP; no story dependency after Foundational.
- **US2 (P1)**: Requires US1 because immutability is verified against posted entries.
- **US3 (P2)**: Requires US1 because retry/concurrency behavior wraps posting.
- **US4 (P2)**: Requires US1 because balances derive from posted entries.
- **US5 (P3)**: Requires US1 and integrates with US3/US4 events for complete audit coverage.

### Within Each User Story

- Write contract, integration, and unit tests first and verify they fail before implementation.
- Domain models before application services.
- Application ports and use cases before adapters.
- Persistence adapters before controller wiring that depends on persisted outcomes.
- Story is complete only when its independent test criteria pass.

### Parallel Opportunities

- Setup package documentation tasks T003-T007 can run in parallel.
- Foundational value object tasks T009-T013 can run in parallel after T008 starts.
- US1 tests T019-T020 and models T022-T023 can run in parallel.
- US2 tests T031-T032 can run in parallel.
- US3 tests T039-T040 and components T042-T043 can run in parallel.
- US4 tests T048-T049 can run in parallel.
- US5 tests T056-T057 can run in parallel.
- Polish documentation tasks T064-T066 can run in parallel.

---

## Parallel Example: User Story 1

```bash
Task: "T019 [US1] Add posting API contract tests in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/PostLedgerTransactionContractTest.java"
Task: "T020 [US1] Add domain invariant tests in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/domain/LedgerTransactionTest.java"
Task: "T022 [US1] Create LedgerEntry domain model in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/domain/model/LedgerEntry.java"
Task: "T023 [US1] Create LedgerTransaction aggregate in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/domain/model/LedgerTransaction.java"
```

## Parallel Example: User Story 3

```bash
Task: "T039 [US3] Add idempotency API contract tests in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/in/web/PostLedgerTransactionIdempotencyContractTest.java"
Task: "T040 [US3] Add idempotency service tests in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/application/IdempotencyServiceTest.java"
Task: "T042 [US3] Create IdempotencyRecord domain model in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/domain/model/IdempotencyRecord.java"
Task: "T043 [US3] Create canonical request hashing service in src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/PostingRequestHasher.java"
```

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 setup.
2. Complete Phase 2 foundational schema, value objects, ports, and error handling.
3. Complete Phase 3 User Story 1.
4. Validate balanced posting, unbalanced rejection, missing debit/credit rejection, and rollback with `./gradlew test`.
5. Stop before additional stories if MVP review is required.

### Incremental Delivery

1. Add US1 to establish atomic balanced posting.
2. Add US2 to lock down immutable history and correction behavior.
3. Add US3 to harden retry, duplicate, conflict, and concurrency behavior.
4. Add US4 to expose derived account balances.
5. Add US5 to complete traceability, audit events, and operational visibility.

### Parallel Team Strategy

1. Complete Setup and Foundational phases together.
2. Assign US2, US3, and US4 after US1 behavior is available.
3. Assign US5 after audit event categories and use case outcomes stabilize.
4. Keep each story independently verified before merging into the next increment.
