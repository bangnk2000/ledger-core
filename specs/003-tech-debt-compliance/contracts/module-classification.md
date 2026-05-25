# Module Classification: Transaction-Critical and Replay-Sensitive

**Feature**: `003-tech-debt-compliance`  
**Date**: 2026-05-19  

This document defines which modules are treated as transaction-critical and
replay-sensitive for the purposes of wave sizing and mandatory validation.

## Classifications

### Transaction-Critical

Code that can directly change posting outcomes, balances, reservations, or
write-side invariants, including explicit transaction boundaries.

**In-scope path prefixes**

- `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/command/`
- `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/application/port/`
- `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/adapter/out/persistence/`
- `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/command/`
- `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/`
- `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/out/persistence/`
- `src/main/resources/db/migration/` (schema can affect correctness and replay)

**Known transaction boundaries**

- `ledger.application.command.PostLedgerTransactionService` (`@Transactional`)
- `ledger.application.query.GetAccountBalanceService` (`@Transactional(readOnly = true)`)
- `ledger.balance.adapter.out.persistence.SpringBalanceTransactionAdapter` (`@Transactional`)

### Replay-Sensitive

Code that can change deterministic rebuild/replay outcomes or ordering.

**In-scope path prefixes**

- `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/BalanceReplayOrderingService.java`
- Any “replay/export” ports and record ordering logic under:
  `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/application/port/out/`

### Normal

All other modules are “normal” unless explicitly promoted by a wave decision.

## Mandatory Validation By Classification

For any change touching `transaction-critical` or `replay-sensitive` modules:

- Must run regression tests relevant to the touched flow.
- Must run deterministic replay validation for the touched flow (or add it in
  Wave 0 as a prerequisite).
- Must run concurrency validation if the change touches locking, retries, or
  protected writes.

For `normal` modules:

- Must run unit tests plus any directly affected integration tests.

## T009 Triage Delta Log (2026-05-19)

- `wave_id`: `NA`
- `backlog_item_id`: `NA`
- `status`: `DONE_WITH_CONCERNS`
- `scope`: Triage executed from available Sonar snapshot only; Qodana remains blocked pending real SARIF artifact.
- `evidence`:
  - Sonar critical findings identified 1 affected path with 2 critical issues:
    `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/adapter/in/web/BalanceApiExceptionHandler.java`.
  - Qodana placeholder baseline still empty (`qodana_runs=0`).
- `decision`:
  - No new transaction boundary discovered from Sonar critical findings.
  - No new replay-sensitive path discovered from Sonar critical findings.
  - The discovered path remains classified as `normal` adapter-in/web concern
    (response payload literal duplication), not transaction-critical and not replay-sensitive.
- `rollback_notes`: `not_applicable` (classification doc only; no code/config mutation).
- `pending_verification`: Re-run classification delta after T007 provides real Qodana SARIF artifact.

