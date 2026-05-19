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

### Balance Module (transaction-critical + replay-sensitive)

- `BAL-REPLAY-001` (seed, replay-sensitive, touches: replay, category: transaction_consistency): Audit replay ordering assumptions and harden determinism boundaries (explicit ordering, stable key comparison); no transaction boundary changes in this item.
- `BAL-CONC-001` (seed, transaction-critical, touches: concurrency, category: concurrency_safety): Evaluate and refactor retry/backoff paths in `ProtectedWriteRetryExecutor` only if required by findings; preserve bounded retry semantics and “fail closed” behavior.
- `BAL-NAME-001` (seed, transaction-critical, touches: none, category: naming_cleanup): Naming consistency cleanup for balance outcome codes/messages only where tooling flags confusing names; no semantic changes.

## Notes

- The seed items are not a substitute for report-driven backlog ingestion; they
  exist to enable early, safe waves while the report ingestion is refined.
- Any item that requires changing `@Transactional` boundaries must be placed
  into a dedicated transaction-boundary wave and cannot be combined with
  concurrency or replay work.

