# Feature Specification: Idempotency Framework

**Feature Branch**: `004-idempotency-framework`  
**Created**: 2026-06-04  
**Status**: Draft  
**Input**: User description: "Create specification for module: idempotency-framework"

## Clarifications

### Session 2026-06-04

- Q: How should fingerprint canonicalization work for duplicate detection? → A: Fingerprint a canonical business-intent object using normalized material fields only, excluding transport metadata.
- Q: What expiration behavior should the framework use for retained idempotency records? → A: Use a replayable retention window followed by a tombstone window that blocks re-execution without requiring indefinite full-outcome replay.
- Q: What lifecycle state machine should the framework use? → A: Use `RECEIVED -> CLAIMED -> PROCESSING -> COMPLETED | REJECTED | INDETERMINATE`, with expiration applied after terminal states as retention handling rather than a primary processing state.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Execute an external operation safely once (Priority: P1)

An application team uses the framework when invoking an external or asynchronous operation that may be retried by clients, schedulers, or message handlers. The framework accepts the operation request, recognizes whether it is the first attempt for the declared idempotency scope, and ensures the business operation is executed at most once for that key and fingerprint combination.

**Why this priority**: Preventing duplicate side effects is the primary value of the framework and is mandatory for financial and integration workflows.

**Independent Test**: Can be fully tested by submitting the same operation twice with the same valid key and confirming only one side effect is permitted while the caller receives a stable outcome.

**Acceptance Scenarios**:

1. **Given** a new idempotency key within its uniqueness boundary, **When** a caller submits a valid operation request, **Then** the framework records the request, allows one execution attempt, and stores an auditable final outcome.
2. **Given** a retry of the same request with the same key and matching fingerprint, **When** the original attempt already completed, **Then** the framework does not execute the business operation again and returns the previously recorded outcome.

---

### User Story 2 - Detect conflicting reuse and concurrent duplicates (Priority: P2)

An application team needs the framework to distinguish legitimate retries from accidental or malicious key reuse. The framework detects when the same key is reused for a different request or when two duplicate attempts arrive at the same time, and it resolves both situations deterministically.

**Why this priority**: Correct replay detection is required to preserve trust in retries and to avoid silent data corruption or duplicate external effects.

**Independent Test**: Can be fully tested by sending concurrent duplicate requests and by reusing one key for materially different requests, then observing deterministic rejection or in-progress behavior without duplicate side effects.

**Acceptance Scenarios**:

1. **Given** two materially identical requests with the same key arriving concurrently, **When** one request has already claimed processing, **Then** the framework prevents a second execution and returns a deterministic duplicate-in-progress result to the competing request.
2. **Given** a request that reuses an existing key with a different fingerprint, **When** the framework validates the request, **Then** it rejects the reuse as a conflict and preserves the original record unchanged.

---

### User Story 3 - Manage retention and expiration consistently (Priority: P3)

An operations or platform team needs idempotency records to age out safely so storage remains bounded while audit expectations remain clear. The framework applies a consistent retention policy, exposes expired-key behavior, and supports cleanup without making active requests unsafe.

**Why this priority**: Retention and cleanup are necessary for long-running platform operation, but they are secondary to correctness of live duplicate prevention.

**Independent Test**: Can be fully tested by advancing records beyond their retention period, running cleanup, and confirming that expired requests follow the declared policy without corrupting active records.

**Acceptance Scenarios**:

1. **Given** an idempotency record whose retention period has elapsed, **When** a cleanup process evaluates it, **Then** the record is marked or removed according to policy without affecting unexpired records.
2. **Given** a caller retries a request after its key has expired, **When** the framework evaluates the key, **Then** it applies the documented expired-key behavior consistently and records the outcome for audit.

---

### Edge Cases

- What happens when a retry arrives while the original request is still processing for longer than expected?
- How does the system handle a request that omits a key where the consuming contract requires one?
- What happens when a caller reuses the same key across different tenants, accounts, or operation scopes?
- How does the system behave when the first attempt produces an indeterminate outcome because the downstream side effect cannot be confirmed?
- What happens when cleanup and a late retry race against each other near the expiration boundary?
- How does the system distinguish retries that are still inside the replay window from late retries that are only protected by a tombstone record?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST provide a reusable idempotency capability that can be invoked by synchronous external requests and asynchronous processing flows.
- **FR-002**: The system MUST require each protected operation to declare its idempotency scope, including the boundary within which a key must be unique.
- **FR-003**: The system MUST validate the presence, format, and allowed lifetime of an idempotency key before any protected side effect begins.
- **FR-004**: The system MUST derive or accept a request fingerprint from a canonical business-intent object using normalized material request fields only, excluding transport metadata, so that legitimate retries remain stable across delivery channels.
- **FR-005**: The system MUST persist an idempotency record before allowing a first execution attempt to proceed.
- **FR-006**: The system MUST ensure that at most one active execution attempt is permitted for a given key and fingerprint within the declared uniqueness boundary.
- **FR-007**: The system MUST detect repeated requests for an existing completed record and return a stable replay outcome without re-running the protected side effect.
- **FR-008**: The system MUST detect reuse of an existing key with a different fingerprint and reject it as a conflict.
- **FR-009**: The system MUST provide deterministic handling for concurrent duplicate requests, including a defined outcome for requests that arrive while the original attempt is still processing.
- **FR-010**: The system MUST record lifecycle state transitions for each idempotency record using `RECEIVED -> CLAIMED -> PROCESSING -> COMPLETED | REJECTED | INDETERMINATE`, with expiration handled after terminal states as retention behavior rather than an active processing transition.
- **FR-011**: The system MUST preserve enough request and outcome metadata for audit, replay investigation, and support diagnostics.
- **FR-012**: The system MUST support an expiration policy with a replayable retention window followed by a tombstone window, defining retention duration, expired-key behavior, cleanup eligibility, and when full stored outcomes may stop being replayed while duplicate re-execution remains blocked.
- **FR-013**: The system MUST provide a storage-facing contract that allows different persistence implementations without changing the business meaning of duplicate detection or replay handling.
- **FR-014**: The system MUST expose result contracts that let consuming modules distinguish first execution, replayed completion, duplicate-in-progress, conflict, expired-key, and indeterminate-outcome cases.
- **FR-015**: The system MUST define behavior for failure scenarios where outcome capture, downstream confirmation, or finalization is incomplete, so retries remain safe and auditable.
- **FR-016**: The system MUST support lookup and duplicate detection at a scale suitable for horizontally scaled consumers operating on the same protected workflow.
- **FR-017**: The system MUST allow future ledger, transaction, webhook, integration, and background-job modules to attach business identifiers and correlation identifiers to idempotency records.
- **FR-018**: The system MUST define integration points for pre-execution claim, post-execution finalization, conflict handling, expiration cleanup, and operational reporting.

### Enterprise Quality Requirements

- **EQR-001**: Critical framework flows MUST define integration-test coverage for first execution, replay, conflict, expiration, and concurrent duplicate handling.
- **EQR-002**: Consumer-facing contracts MUST remain backward compatible so future modules can adopt the framework without breaking existing request semantics.
- **EQR-003**: Any persistence schema introduced for idempotency records MUST support zero-downtime deployment and a rollback or roll-forward strategy.
- **EQR-004**: The framework MUST define retry, timeout, and failure-handling semantics for protected operations whose final outcome may be delayed or uncertain.
- **EQR-005**: The framework MUST define authorization and validation expectations for who may create, inspect, or replay idempotency-protected operations when exposed through future APIs.
- **EQR-006**: The framework MUST emit structured audit signals for claim, replay, conflict, completion, expiration, and indeterminate-outcome events.
- **EQR-007**: The framework MUST state measurable concurrency and throughput expectations for horizontally scaled operation without duplicate side effects.
- **EQR-008**: When used for ledger-affecting workflows, the framework MUST preserve immutable audit history and prevent duplicate mutation attempts from bypassing financial controls.
- **EQR-009**: All mutating external requests using the framework MUST define stable retry outcomes and duplicate-handling behavior.
- **EQR-010**: Framework integration contracts MUST define explicit transaction boundaries between claiming a key, executing business work, and finalizing the record.
- **EQR-011**: Any major architectural deviation or mandatory new dependency introduced to realize the framework MUST be documented through an ADR or equivalent justification.

### Key Entities *(include if feature involves data)*

- **Idempotency Key**: A caller-supplied identifier used to associate retry attempts with one protected operation inside a declared uniqueness boundary.
- **Idempotency Scope**: The business boundary that determines where a key must be unique, such as a tenant, account, operation family, or workflow instance.
- **Request Fingerprint**: A stable representation of a canonical business-intent object composed only of normalized material request fields, excluding transport metadata, used to verify whether a retry matches the original intent.
- **Idempotency Record**: The durable record that tracks the key, scope, fingerprint, lifecycle state, timestamps, audit metadata, and terminal outcome for a protected operation.
- **Execution Outcome**: The canonical result classification recorded for a protected operation, including completion, conflict, duplicate-in-progress, expiration, or indeterminate status.

### Lifecycle States

- **Received**: The framework has accepted the request for validation and uniqueness checks, but no execution claim has been granted yet.
- **Claimed**: The framework has reserved the key for one active execution attempt within the declared scope.
- **Processing**: The protected business operation is in progress and duplicate requests must not start a second execution.
- **Completed**: The protected operation reached a terminal successful outcome that can be replayed safely to duplicate requests.
- **Rejected**: The request was denied because of invalid key format, key conflict, or other pre-execution policy failure.
- **Indeterminate**: The framework cannot yet prove whether the protected side effect completed, so follow-up retries must use a defined safe resolution path.
- **Expired Retention Status**: After a record reaches a terminal state, it may move out of the replay window and into tombstone-only retention; this is a retention handling status rather than a primary processing lifecycle state.

### Invariants

- A key is evaluated only within its declared uniqueness boundary; the same raw key value may exist in separate boundaries without collision.
- A matching key and fingerprint combination may have at most one active execution attempt at any time.
- A conflicting fingerprint may never overwrite or mutate the original accepted request record for the same key and scope.
- Once a terminal outcome is recorded, later duplicate requests must not trigger a new side effect for that record.
- Terminal processing states are limited to `COMPLETED`, `REJECTED`, and `INDETERMINATE`; expiration may change replay behavior after a terminal state but must not reactivate processing.
- Lifecycle transitions must be auditable and must preserve the history needed to explain why a request executed, replayed, conflicted, expired, or became indeterminate.

### Failure Scenarios

- The framework must define how to respond when record creation succeeds but the protected operation never starts.
- The framework must define how to respond when the protected operation may have completed but final outcome capture fails.
- The framework must define how retries behave when the original attempt is stuck in processing beyond its expected completion window.
- The framework must define how cleanup interacts with records whose final outcome is still indeterminate and must not let expiration handling implicitly convert active processing into a terminal success or fresh retry.

### Concurrency Guarantees

- Concurrent duplicate requests competing for the same key and fingerprint must resolve so that only one request gains the active execution claim.
- Requests arriving after a claim is granted but before finalization must receive a stable duplicate-in-progress outcome rather than triggering parallel work.
- Consumers operating on multiple service instances must observe the same duplicate-detection result for the same protected request scope.

### Integration Contracts

- **Claim Contract**: Consuming modules can request a uniqueness decision before starting side effects and receive a classification of first attempt, replay, conflict, or duplicate-in-progress.
- **Finalize Contract**: Consuming modules can record a terminal or indeterminate outcome after business processing completes or becomes uncertain.
- **Replay Contract**: Consuming modules can obtain the previously recorded outcome for a completed request without re-running business logic.
- **Cleanup Contract**: Platform operations can identify records eligible for expiration handling and retention cleanup according to policy.
- **Audit Contract**: Support, compliance, and downstream modules can inspect the lifecycle trail for protected requests using durable identifiers and correlation metadata.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In validation scenarios, 100% of repeated requests with the same valid key and matching fingerprint produce no additional side effects.
- **SC-002**: In validation scenarios, 100% of conflicting reuses of the same key with a different fingerprint are detected and rejected with a deterministic outcome.
- **SC-003**: In concurrent duplicate validation scenarios, exactly one request is allowed to proceed as the active execution for a given key and scope, while all competing requests receive a stable non-executing result.
- **SC-004**: Platform teams can apply one shared framework contract to synchronous and asynchronous protected operations without defining new duplicate-handling rules per integration.
- **SC-005**: Audit reviewers can trace every protected request from first receipt to terminal or indeterminate outcome using the idempotency record alone.
- **SC-006**: Expiration policy behavior is consistent enough that support and operations teams can determine whether a late retry will replay, conflict, or be treated as a new attempt based on documented retention rules.

## Assumptions

- The first version of the specification targets a shared platform capability rather than a single business workflow.
- Payment processing rules, business-specific transaction decisions, and API gateway concerns remain outside this framework and will be supplied by consuming modules.
- Consumers of the framework will explicitly declare which operations require idempotency protection instead of assuming all requests are protected by default.
- The framework will be used in multi-tenant and multi-workflow contexts, so uniqueness boundaries must be configurable rather than globally fixed.
- Expired-key behavior should use a replay window followed by a tombstone window, while the exact durations may vary by consuming workflow class.
- Indeterminate downstream outcomes are expected to occur in some integrations, so the framework must preserve a safe recovery path instead of assuming every operation ends in a clean success or failure.
