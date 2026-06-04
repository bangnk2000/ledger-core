# Tasks: Audit Trail Framework

**Input**: Design documents from `/specs/005-audit-trail-framework/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Integration tests are REQUIRED for every critical business flow. Contract, unit, characterization, and additional integration tests MUST be included to validate immutable capture, non-blocking publication, authorized investigation queries, retention transitions, integrity verification, and ledger/idempotency linkage behavior.

**Execution Workflow**: Every generated task list MUST be executed using the mandatory workflow order from the constitution: worktree -> TDD (red-green-refactor) -> subagent-driven execution -> code review -> finish-branch.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this belongs to (`[US1]`, `[US2]`, `[US3]`)
- Include exact file paths in descriptions

## Phase 0: Worktree and TDD Harness

**Purpose**: Establish isolated execution, test-first discipline, and subagent-ready decomposition before implementation starts

- [X] T000 Create an isolated worktree or equivalent isolated workspace for feature branch `005-audit-trail-framework`
- [X] T001 Capture red-green-refactor and final verification commands in `specs/005-audit-trail-framework/quickstart.md`
- [X] T002 Plan subagent-driven execution slices and ownership in `specs/005-audit-trail-framework/tasks.md`

### Execution Slices

- Slice A: audit module domain and application contracts (`src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/`)
- Slice B: audit persistence and query adapters (`src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/out/persistence/`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/in/web/`)
- Slice C: consumer onboarding and characterization for ledger/balance integrations (`src/main/java/com/bangnk/ledgercore/ledger_core/ledger/`, `src/main/java/com/bangnk/ledgercore/ledger_core/balance/`, matching tests under `src/test/java/com/bangnk/ledgercore/ledger_core/ledger/` and `src/test/java/com/bangnk/ledgercore/ledger_core/balance/`)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create the package skeleton and test locations used by all stories

- [X] T003 Create the `audit` module package structure with `package-info.java` files under `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/in/web/`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/out/persistence/`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/port/in/`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/port/out/`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/config/`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/model/`, and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/valueobject/`
- [X] T004 Create the `audit` test package structure with `package-info.java` files under `src/test/java/com/bangnk/ledgercore/ledger_core/audit/domain/`, `src/test/java/com/bangnk/ledgercore/ledger_core/audit/application/`, `src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/`, and `src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/in/web/`
- [X] T005 [P] Add focused verification command examples for the audit module to `specs/005-audit-trail-framework/quickstart.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T006 Add additive Flyway schema for immutable audit events, integrity metadata, publication backlog rows, and investigation indexes in `src/main/resources/db/migration/V4__create_audit_trail_framework.sql`
- [X] T007 [P] Create core value objects for actor, trace, ledger references, idempotency references, integrity proof, and retention policy in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/valueobject/ActorIdentity.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/valueobject/TraceContext.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/valueobject/LedgerTransactionReference.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/valueobject/IdempotencyReference.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/valueobject/IntegrityProof.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/valueobject/AuditRetentionPolicyProfile.java`
- [X] T008 [P] Create domain models and enums for immutable events, publication attempts, and investigation summaries in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/model/AuditEvent.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/model/PublicationAttempt.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/model/InvestigationView.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/model/AuditEnums.java`
- [X] T009 [P] Define inbound use-case contracts for capture, investigation query, publication recovery, retention processing, and integrity verification in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/port/in/AuditCaptureUseCase.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/port/in/AuditInvestigationQueryUseCase.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/port/in/AuditPublicationRecoveryUseCase.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/port/in/AuditRetentionUseCase.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/port/in/AuditIntegrityVerificationUseCase.java`
- [X] T010 [P] Define outbound persistence and observability ports in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/port/out/AuditEventRepositoryPort.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/port/out/AuditPublicationRepositoryPort.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/port/out/AuditInvestigationRepositoryPort.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/port/out/AuditIntegrityRepositoryPort.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/port/out/AuditObservabilityPort.java`
- [X] T011 Create shared application request, result, and error contracts in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditCaptureCommand.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditQueryCriteria.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditApplicationErrors.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditDecisionTypes.java`
- [X] T012 Create Spring wiring for the new module in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/config/AuditModuleConfiguration.java`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Capture every critical state change (Priority: P1) 🎯 MVP

**Goal**: Persist immutable audit events with normalized actor/trace data and explicit ledger/idempotency linkage while keeping downstream publication off the business success path.

**Independent Test**: Execute a business-critical state change in an onboarded flow, verify that an immutable audit record is stored with actor, trace, and linkage data, and verify that business success is preserved when publication is deferred.

### Tests for User Story 1 ⚠️

- [X] T013 [P] [US1] Add domain tests for immutable event creation, explicit absence handling, and linkage invariants in `src/test/java/com/bangnk/ledgercore/ledger_core/audit/domain/AuditEventTest.java`
- [X] T014 [P] [US1] Add application tests for durable capture, publication deferment, and actor/trace normalization in `src/test/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditCaptureServiceTest.java`
- [X] T015 [P] [US1] Add PostgreSQL integration tests for immutable event persistence and publication backlog creation in `src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/AuditPersistenceIntegrationTest.java` and `src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/AuditPublicationIntegrationTest.java`
- [X] T016 [P] [US1] Add characterization tests for ledger and balance onboarding capture behavior in `src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/AuditFrameworkLedgerCharacterizationTest.java` and `src/test/java/com/bangnk/ledgercore/ledger_core/balance/adapter/AuditFrameworkBalanceCharacterizationTest.java`

### Implementation for User Story 1

- [X] T017 [US1] Implement immutable event construction and absence-preserving linkage rules in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/model/AuditEvent.java`
- [X] T018 [US1] Implement capture and publication-deferment application services in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditCaptureService.java` and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditPublicationRecoveryService.java`
- [X] T019 [US1] Implement JPA entities and repositories for audit events and publication attempts in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/out/persistence/AuditEventJpaEntity.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/out/persistence/PublicationAttemptJpaEntity.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/out/persistence/SharedAuditEventJpaRepository.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/out/persistence/SharedPublicationAttemptJpaRepository.java`
- [X] T020 [US1] Implement PostgreSQL-backed capture and publication adapters in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/out/persistence/JpaAuditEventRepositoryAdapter.java` and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/out/persistence/JpaAuditPublicationRepositoryAdapter.java`
- [X] T021 [US1] Implement ledger and balance audit translators in `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/LedgerAuditTranslator.java` and `src/main/java/com/bangnk/ledgercore/ledger_core/balance/application/BalanceAuditTranslator.java`
- [X] T022 [US1] Integrate shared audit capture into critical ledger and balance flows in `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/PostLedgerTransactionService.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/balance/application/ReserveFundsService.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/balance/application/ConfirmReservationUseCase.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/balance/application/ReleaseReservationUseCase.java`
- [X] T023 [US1] Add structured observability for capture success, deferred publication, and missing metadata handling in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditObservability.java`

**Checkpoint**: User Story 1 should store immutable audit evidence for onboarded critical flows without letting publication failures fail the business operation

---

## Phase 4: User Story 2 - Investigate and reconstruct activity (Priority: P2)

**Goal**: Provide authorized investigation queries that reconstruct cross-module event history by business subject, actor, trace, ledger transaction, and idempotency linkage.

**Independent Test**: Generate related events across ledger and balance flows, query them by correlation identifier or linked object reference, and verify the ordered investigation view reconstructs the workflow without using non-audit logs.

### Tests for User Story 2 ⚠️

- [ ] T024 [P] [US2] Add contract tests for the audit investigation query API in `src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/in/web/AuditInvestigationContractTest.java`
- [ ] T025 [P] [US2] Add application tests for ordered investigation reconstruction and redaction behavior in `src/test/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditInvestigationServiceTest.java`
- [ ] T026 [P] [US2] Add PostgreSQL integration tests for filtering by subject, trace, ledger transaction, and idempotency linkage in `src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/AuditInvestigationIntegrationTest.java`
- [ ] T027 [P] [US2] Add cross-module characterization tests for workflow reconstruction in `src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/AuditInvestigationLedgerCharacterizationTest.java` and `src/test/java/com/bangnk/ledgercore/ledger_core/balance/adapter/AuditInvestigationBalanceCharacterizationTest.java`

### Implementation for User Story 2

- [ ] T028 [US2] Implement investigation query and ordering behavior in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditInvestigationService.java`
- [ ] T029 [US2] Implement investigation query repository adapter and search projections in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/out/persistence/JpaAuditInvestigationRepositoryAdapter.java` and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/out/persistence/AuditInvestigationProjection.java`
- [ ] T030 [US2] Implement the authorized audit query web adapter in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/in/web/AuditInvestigationController.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/in/web/AuditQueryDtos.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/in/web/AuditApiExceptionHandler.java`
- [ ] T031 [US2] Add role-scoped access checks and sensitive field redaction rules in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditAccessPolicyService.java` and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/in/web/AuditInvestigationController.java`
- [ ] T032 [US2] Add query observability for investigation access, filter usage, and reconstruction outcomes in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditObservability.java`

**Checkpoint**: User Story 2 should return an authorized, ordered audit history that reconstructs workflows through business, trace, ledger, and idempotency references

---

## Phase 5: User Story 3 - Govern retention and evidence integrity (Priority: P3)

**Goal**: Enforce staged retention and tamper-evident verification so records remain trustworthy and policy-compliant throughout their lifecycle.

**Independent Test**: Capture audit events, verify integrity metadata detects unauthorized mutation attempts, advance records through active and restricted retention, and confirm disposition preserves required evidence metadata.

### Tests for User Story 3 ⚠️

- [ ] T033 [P] [US3] Add domain tests for retention transitions and integrity-proof invariants in `src/test/java/com/bangnk/ledgercore/ledger_core/audit/domain/AuditRetentionPolicyProfileTest.java` and `src/test/java/com/bangnk/ledgercore/ledger_core/audit/domain/IntegrityProofTest.java`
- [ ] T034 [P] [US3] Add application tests for retention processing and integrity verification outcomes in `src/test/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditRetentionServiceTest.java` and `src/test/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditIntegrityVerificationServiceTest.java`
- [ ] T035 [P] [US3] Add PostgreSQL integration tests for retention transitions, proof persistence, and verification failure surfacing in `src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/AuditRetentionIntegrationTest.java` and `src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/AuditIntegrityVerificationIntegrationTest.java`

### Implementation for User Story 3

- [ ] T036 [US3] Implement staged retention and integrity-proof behavior in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/valueobject/AuditRetentionPolicyProfile.java`, `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/valueobject/IntegrityProof.java`, and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/domain/model/AuditEvent.java`
- [ ] T037 [US3] Implement retention processing and integrity verification services in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditRetentionService.java` and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditIntegrityVerificationService.java`
- [ ] T038 [US3] Add persistence queries and lifecycle updates for retention stages and verification state in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/out/persistence/JpaAuditEventRepositoryAdapter.java` and `src/main/java/com/bangnk/ledgercore/ledger_core/audit/adapter/out/persistence/JpaAuditIntegrityRepositoryAdapter.java`
- [ ] T039 [US3] Add retention-transition and integrity-failure observability in `src/main/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditObservability.java`

**Checkpoint**: User Story 3 should preserve tamper-evident trust and policy-compliant retention without mutating historical audit content

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Complete rollout guidance, migration validation, review, and branch-finish readiness across all stories

- [ ] T040 [P] Update implementation notes and verification evidence in `specs/005-audit-trail-framework/plan.md`, `specs/005-audit-trail-framework/research.md`, `specs/005-audit-trail-framework/data-model.md`, and `specs/005-audit-trail-framework/quickstart.md`
- [ ] T041 Validate zero-downtime migration, rollback or roll-forward strategy, and coexistence with existing ledger structured audit logging in `src/main/resources/db/migration/V4__create_audit_trail_framework.sql` and `specs/005-audit-trail-framework/contracts/consumer-onboarding-contract.md`
- [ ] T042 [P] Add any remaining adapter-level regression coverage for ledger/idempotency linkage and authorization edge cases in `src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/`, `src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/`, and `src/test/java/com/bangnk/ledgercore/ledger_core/balance/adapter/`
- [ ] T043 Run the full verification suite from `specs/005-audit-trail-framework/quickstart.md` and record results in `specs/005-audit-trail-framework/quickstart.md`
- [ ] T044 Run code review, resolve or record all findings, and capture the finish-branch decision in `specs/005-audit-trail-framework/tasks.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 0**: Starts immediately and establishes worktree/TDD discipline for all later work
- **Phase 1**: Depends on Phase 0 and creates the shared source/test structure
- **Phase 2**: Depends on Phase 1 and blocks all user story work
- **Phase 3 (US1)**: Depends on Phase 2 and delivers the MVP immutable capture capability
- **Phase 4 (US2)**: Depends on Phase 3 because investigation queries require captured audit data and shared linkage semantics
- **Phase 5 (US3)**: Depends on Phase 3 because retention and integrity operate on durable captured events
- **Phase 6**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Starts after foundational work and delivers the MVP
- **User Story 2 (P2)**: Depends on User Story 1 durable capture and onboarding translations
- **User Story 3 (P3)**: Depends on User Story 1 immutable capture and shares persistence/investigation primitives with User Story 2

### Within Each User Story

- Tests MUST be written and observed failing before implementation
- Domain/value objects before application services
- Application services before persistence or web adapters
- Persistence behavior before consumer-characterization validation
- Observability and compatibility checks before considering the story complete

### Parallel Opportunities

- Phase 1 tasks marked `[P]` can run in parallel
- In Phase 2, value objects, domain models, and port definitions marked `[P]` can run in parallel
- In each user story, test tasks marked `[P]` can run in parallel
- Ledger and balance characterization work can run in parallel once the shared audit contracts exist

---

## Parallel Example: User Story 1

```bash
# Launch US1 tests together:
Task: "T013 [P] [US1] Add domain tests for immutable event creation, explicit absence handling, and linkage invariants in src/test/java/com/bangnk/ledgercore/ledger_core/audit/domain/AuditEventTest.java"
Task: "T014 [P] [US1] Add application tests for durable capture, publication deferment, and actor/trace normalization in src/test/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditCaptureServiceTest.java"
Task: "T015 [P] [US1] Add PostgreSQL integration tests for immutable event persistence and publication backlog creation in src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/AuditPersistenceIntegrationTest.java and src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/AuditPublicationIntegrationTest.java"
Task: "T016 [P] [US1] Add characterization tests for ledger and balance onboarding capture behavior in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/AuditFrameworkLedgerCharacterizationTest.java and src/test/java/com/bangnk/ledgercore/ledger_core/balance/adapter/AuditFrameworkBalanceCharacterizationTest.java"
```

---

## Parallel Example: User Story 2

```bash
# Launch US2 query tests together:
Task: "T024 [P] [US2] Add contract tests for the audit investigation query API in src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/in/web/AuditInvestigationContractTest.java"
Task: "T025 [P] [US2] Add application tests for ordered investigation reconstruction and redaction behavior in src/test/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditInvestigationServiceTest.java"
Task: "T026 [P] [US2] Add PostgreSQL integration tests for filtering by subject, trace, ledger transaction, and idempotency linkage in src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/AuditInvestigationIntegrationTest.java"
Task: "T027 [P] [US2] Add cross-module characterization tests for workflow reconstruction in src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/AuditInvestigationLedgerCharacterizationTest.java and src/test/java/com/bangnk/ledgercore/ledger_core/balance/adapter/AuditInvestigationBalanceCharacterizationTest.java"
```

---

## Parallel Example: User Story 3

```bash
# Launch US3 retention and integrity tests together:
Task: "T033 [P] [US3] Add domain tests for retention transitions and integrity-proof invariants in src/test/java/com/bangnk/ledgercore/ledger_core/audit/domain/AuditRetentionPolicyProfileTest.java and src/test/java/com/bangnk/ledgercore/ledger_core/audit/domain/IntegrityProofTest.java"
Task: "T034 [P] [US3] Add application tests for retention processing and integrity verification outcomes in src/test/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditRetentionServiceTest.java and src/test/java/com/bangnk/ledgercore/ledger_core/audit/application/AuditIntegrityVerificationServiceTest.java"
Task: "T035 [P] [US3] Add PostgreSQL integration tests for retention transitions, proof persistence, and verification failure surfacing in src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/AuditRetentionIntegrationTest.java and src/test/java/com/bangnk/ledgercore/ledger_core/audit/adapter/AuditIntegrityVerificationIntegrationTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 0
2. Complete Phase 1
3. Complete Phase 2
4. Complete Phase 3
5. **STOP and VALIDATE**: verify that onboarded ledger/balance critical flows store immutable audit evidence without failing when publication is deferred

### Incremental Delivery

1. Deliver shared immutable capture and non-blocking publication behavior through User Story 1
2. Add investigation query and workflow reconstruction behavior in User Story 2
3. Add retention and integrity verification behavior in User Story 3
4. Finish with migration validation, full verification, code review, and finish-branch capture in Phase 6
