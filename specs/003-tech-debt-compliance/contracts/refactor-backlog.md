# Refactor Backlog (Seed + Triage Process)

**Feature**: `003-tech-debt-compliance`  
**Date**: 2026-05-19  

This backlog is intentionally incremental. It starts with a “seed” set of known
high-value items, plus a repeatable triage process to convert Sonar/Qodana
reports into wave-sized backlog items.

## Backlog Triage Process (mandatory)

1. Pull the current report snapshots:
   - Sonar: `tools/sonar/fetch-sonar-report.sh` -> `build/reports/sonar-report.json`
   - Qodana: CI results from `.github/workflows/qodana_code_quality.yml` (or a local Qodana run)
2. Normalize findings into backlog items:
   - One `RefactorBacklogItem` = one module group + one category.
   - Record `finding_id` and affected paths in the item description.
3. Classify each item:
   - `module_id`, `category`, `risk`, and `touches` (exactly one).
4. Prioritize:
   - transaction-critical + replay-sensitive first
   - then other in-scope modules
5. Define acceptance checks:
   - always include targeted tests
   - include replay/concurrency validation for critical modules per policy

## Seed Backlog Items (initial)

### CI / Enforcement (Wave 0 prerequisites)

- `CI-SONAR-001` (`sonar`, normal, touches: none): Ensure CI runs the intended Sonar analysis task and fails on quality gate failure; remove redundant double build in `.github/workflows/sonarqube.yml`.
- `CI-QODANA-001` (`qodana`, normal, touches: none): Fix Qodana workflow checkout so push events do not rely on PR-only context (`github.event.pull_request.head.sha`).
- `CI-POLICY-001` (manual, normal, touches: none): Implement “block all new critical findings” in CI with time-bound exceptions recorded in `contracts/quality-gate-policy.md`.

### Ledger Foundation (transaction-critical)

- `LEDGER-TX-001` (seed, transaction-critical, touches: none, category: long_methods): Reduce cognitive complexity of `PostLedgerTransactionService.post(...)` via extraction of pure helper methods while preserving ordering, audit events, and idempotency behavior.
- `LEDGER-QRY-001` (seed, transaction-critical, touches: none, category: duplication): Remove duplicated null-handling patterns in `GetAccountBalanceService` without changing money scale rules or audit event payload.

## Wave W1-W2 Completion Evidence (2026-05-19)

- `LEDGER-TX-001`
  - `wave_id`: `W1`
  - `backlog_item_id`: `LEDGER-TX-001`
  - `status`: `DONE`
  - `evidence`:
    - Characterization: `src/test/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/PostLedgerTransactionCharacterizationTest.java`
    - Refactor: `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/PostLedgerTransactionService.java`
    - Validation command: `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.PostLedgerTransactionCharacterizationTest --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.PostLedgerTransactionIntegrationTest --tests com.bangnk.ledgercore.ledger_core.ledger.application.query.GetAccountBalanceServiceCharacterizationTest --tests com.bangnk.ledgercore.ledger_core.ledger.application.GetAccountBalanceServiceTest --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.GetAccountBalanceIntegrationTest --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web.GetAccountBalanceContractTest --tests com.bangnk.ledgercore.ledger_core.ledger.application.AuditEventPublisherTest`
    - Validation result: `BUILD SUCCESSFUL` on 2026-05-19 (after sandbox rerun with elevated permissions)
  - `pending_verification`: Commit linkage not captured in this record; verify via branch history when preparing merge evidence.

- `LEDGER-QRY-001`
  - `wave_id`: `W2`
  - `backlog_item_id`: `LEDGER-QRY-001`
  - `status`: `DONE`
  - `evidence`:
    - Characterization: `src/test/java/com/bangnk/ledgercore/ledger_core/ledger/application/query/GetAccountBalanceServiceCharacterizationTest.java`
    - Refactor: `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/query/GetAccountBalanceService.java`
    - Validation command: `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.PostLedgerTransactionCharacterizationTest --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.PostLedgerTransactionIntegrationTest --tests com.bangnk.ledgercore.ledger_core.ledger.application.query.GetAccountBalanceServiceCharacterizationTest --tests com.bangnk.ledgercore.ledger_core.ledger.application.GetAccountBalanceServiceTest --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.GetAccountBalanceIntegrationTest --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web.GetAccountBalanceContractTest --tests com.bangnk.ledgercore.ledger_core.ledger.application.AuditEventPublisherTest`
    - Validation result: `BUILD SUCCESSFUL` on 2026-05-19 (after sandbox rerun with elevated permissions)
  - `pending_verification`: Commit linkage not captured in this record; verify via branch history when preparing merge evidence.

### Balance Module (transaction-critical + replay-sensitive)

- `BAL-REPLAY-001` (seed, replay-sensitive, touches: replay, category: transaction_consistency): Audit replay ordering assumptions and harden determinism boundaries (explicit ordering, stable key comparison); no transaction boundary changes in this item.
- `BAL-CONC-001` (seed, transaction-critical, touches: concurrency, category: concurrency_safety): Evaluate and refactor retry/backoff paths in `ProtectedWriteRetryExecutor` only if required by findings; preserve bounded retry semantics and “fail closed” behavior.
- `BAL-NAME-001` (seed, transaction-critical, touches: none, category: naming_cleanup): Naming consistency cleanup for balance outcome codes/messages only where tooling flags confusing names; no semantic changes.

## Wave W3 Completion Evidence (2026-05-19)

- `BAL-MUT-001`
  - `wave_id`: `W3`
  - `backlog_item_id`: `BAL-MUT-001`
  - `status`: `DONE`
  - `scope`: Characterize reserve mutation outcomes and refactor long reserve mutation flow through helper extraction only.
  - `guardrails`:
    - No transaction boundary changes (`reserve(...)` still executes through `retryExecutor.execute(() -> transactionPort.withinProtectedWrite(...))`).
    - No replay-ordering behavior touched.
    - No retry/fail-closed behavior changes.
  - `evidence`:
    - Characterization: `src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceMutationCharacterizationTest.java`
    - Refactor: `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/ReserveFundsService.java`
    - Validation command: `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.application.BalanceMutationCharacterizationTest --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReplayRebuildIntegrationTest --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationIntegrationTest --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationConcurrencyIntegrationTest`
    - Validation result: `BUILD SUCCESSFUL` on 2026-05-19 (after sandbox rerun with elevated permissions due `Could not determine a usable wildcard IP for this machine`)
  - `pending_verification`: Commit linkage not captured in this record; verify via branch history when preparing merge evidence.

## Wave W4 Completion Evidence (2026-05-19)

- `BAL-REPLAY-001`
  - `wave_id`: `W4`
  - `backlog_item_id`: `BAL-REPLAY-001`
  - `status`: `DONE`
  - `scope`: Audited replay ordering boundaries and hardened deterministic ordering with explicit comparator definitions only.
  - `guardrails`:
    - No `@Transactional` or transaction boundary changes.
    - No replay payload mutation behavior changes.
  - `evidence`:
    - Refactor: `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceReplayOrderingService.java`
    - Comparator hardening: explicit `Comparator.naturalOrder()` for transaction and entry tie-break keys.
    - Validation command (sandbox attempt): `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReplayDeterminismIntegrationTest`
    - Validation result (sandbox attempt): `FAILURE` with `Could not determine a usable wildcard IP for this machine`
    - Validation command (elevated rerun): `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReplayDeterminismIntegrationTest`
    - Validation result (elevated rerun): `BUILD SUCCESSFUL` on 2026-05-19
    - New determinism findings: none

## Wave W5 Completion Evidence (2026-05-19)

- `BAL-CONC-001`
  - `wave_id`: `W5`
  - `backlog_item_id`: `BAL-CONC-001`
  - `status`: `DONE`
  - `scope`: Triaged concurrency behavior for lock ordering/retry/backoff/isolation and validated current protected-write retry path.
  - `triage_findings`:
    - Lock ordering: no inversion introduced in current reserve/protected-write flow.
    - Retry semantics: bounded retries and fail-closed timeout path are preserved.
    - Backoff behavior: existing retry policy backoff behavior is intact.
    - Transaction isolation: no transaction-boundary changes required for this wave.
  - `decision`: No code refactor required in `ProtectedWriteRetryExecutor` for this wave; behavior already matches constraints.
  - `evidence`:
    - Direct class-level proof (no-op decision target): `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/ProtectedWriteRetryExecutor.java` remains unchanged in this wave and still enforces bounded retry + fail-closed timeout semantics.
    - Direct executor coverage: `src/test/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/BalanceContentionRetryIntegrationTest.java` validates success-after-contention and timeout/fail-closed behavior for `ProtectedWriteRetryExecutor`.
    - Concurrency validation command (sandbox attempt): `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationConcurrencyIntegrationTest`
    - Concurrency validation result (sandbox attempt): `FAILURE` with `Could not determine a usable wildcard IP for this machine`
    - Concurrency validation command (elevated rerun): `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationConcurrencyIntegrationTest`
    - Concurrency validation result (elevated rerun): `BUILD SUCCESSFUL` on 2026-05-19
    - Protected-write regression command (sandbox attempt): `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationIntegrationTest --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceContentionRetryIntegrationTest`
    - Protected-write regression result (sandbox attempt): `FAILURE` with `Could not determine a usable wildcard IP for this machine`
    - Protected-write regression command (elevated rerun): `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationIntegrationTest --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceContentionRetryIntegrationTest`
    - Protected-write regression result (elevated rerun): `BUILD SUCCESSFUL` on 2026-05-19

## Wave W6 Decision Evidence (2026-05-19)

- `W6-TX-DECISION-20260519`
  - `wave_id`: `W6`
  - `backlog_item_id`: `W6-TX-DECISION-20260519`
  - `status`: `DONE_WITH_CONCERNS`
  - `scope`: Determine whether any remaining findings require transaction-boundary changes (`touches: transaction_boundary`).
  - `evidence`:
    - Sonar snapshot source: `build/reports/sonar-report.json`.
    - Critical findings ingested: only `SONAR-CRIT-20260519-001` and `SONAR-CRIT-20260519-002`.
    - Both critical findings are `java:S1192` duplicated literals in `BalanceApiExceptionHandler` and explicitly classified as `touches: none`.
    - No backlog items are currently classified as `touches: transaction_boundary`.
  - `decision`: W6 implementation tasks that would modify `@Transactional` boundaries are not required for the current Sonar-only backlog; skip risky boundary changes.
  - `rollback_notes`: Not applicable because no transaction-boundary code change was introduced.
  - `pending_verification`: Not applicable; decision was made from available local Sonar snapshot.

## Notes

- The seed items are not a substitute for report-driven backlog ingestion; they
  exist to enable early, safe waves while the report ingestion is refined.
- Any item that requires changing `@Transactional` boundaries must be placed
  into a dedicated transaction-boundary wave and cannot be combined with
  concurrency or replay work.

## T008 Ingestion Status (2026-05-19)

- `wave_id`: `NA`
- `backlog_item_id`: `NA`
- `status`: `DONE_WITH_CONCERNS`
- `scope`: Sonar critical findings normalized from `build/reports/sonar-report.json`; Qodana critical ingestion remains blocked until a real SARIF snapshot replaces placeholder baseline.
- `evidence`:
  - Sonar parse: `total=50`, `critical_count=2`, `major_count=48`.
  - Qodana parse: `qodana_runs=0` in `tools/qodana/qodana.sarif.json`.
- `decision`: Add stable Sonar-derived critical backlog items now; append Qodana-derived items in a follow-up once T007 is unblocked.
- `rollback_notes`: `not_applicable` (documentation/triage record only).
- `pending_verification`: Qodana ingestion evidence unavailable (`qodana_runs=0`); follow-up required after T007 unblocks real SARIF evidence.

## Sonar Critical Ingestion (Wave W0 backlog input)

- `SONAR-CRIT-20260519-001`
  - `source_finding_id`: `AZ42yAV8brWrxHmevRyF`
  - `source_rule`: `java:S1192`
  - `module_id`: `ledger.balance.adapter.in.web`
  - `category`: `duplication_literals`
  - `risk`: `normal`
  - `touches`: `none`
  - `paths`:
    - `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceApiExceptionHandler.java:18`
  - `summary`: Extract duplicated `"message"` literal into a shared constant to reduce repeated error payload keys.
  - `acceptance_checks`:
    - Keep API response field names backward-compatible (`outcome`, `message` unchanged).
    - Run focused contract tests covering balance API error payload shape.
    - Verify no transaction boundary annotations are changed.

- `SONAR-CRIT-20260519-002`
  - `source_finding_id`: `AZ42yAV8brWrxHmevRyG`
  - `source_rule`: `java:S1192`
  - `module_id`: `ledger.balance.adapter.in.web`
  - `category`: `duplication_literals`
  - `risk`: `normal`
  - `touches`: `none`
  - `paths`:
    - `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceApiExceptionHandler.java:18`
  - `summary`: Extract duplicated `"outcome"` literal into a shared constant to reduce repeated error payload keys.
  - `acceptance_checks`:
    - Keep API response field names backward-compatible (`outcome`, `message` unchanged).
    - Run focused contract tests covering balance API error payload shape.
    - Verify no transaction boundary annotations are changed.

