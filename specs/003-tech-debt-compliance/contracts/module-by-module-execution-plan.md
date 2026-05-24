# Module-by-Module Execution Plan (Phased Waves)

**Feature**: `003-tech-debt-compliance`  
**Date**: 2026-05-19  

This plan applies the "small waves" rule: one module group + one refactor
category per wave, with strict separation of transaction-boundary, concurrency,
and replay-related refactors.

## Execution Location and Branch Strategy

- **Primary worktree**: `/home/bangnk/projects/ledger-core/.worktrees/003-tech-debt-compliance-impl`
- **Implementation branch**: `003-tech-debt-compliance-impl`
- **Wave branch policy**:
  - Keep `003-tech-debt-compliance-impl` as the integration branch for this feature line.
  - Execute one wave at a time with isolated commits and explicit evidence links.
  - If an emergency rollback is needed, revert only the wave commit(s) without mixing unrelated files.
  - Never mix `transaction_boundary` changes with `concurrency` or `replay` touches in the same wave.

## Modules In Scope (priority ordered)

1. `ledger` foundation (transaction-critical)
   - `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/**`
2. `ledger/balance` bounded context (transaction-critical + replay-sensitive)
   - `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance/**`
3. CI/static-analysis wiring (enforcement)
   - `.github/workflows/**`, `qodana.yaml`, `build.gradle`, `tools/sonar/**`

Classification details: `contracts/module-classification.md`.

## Wave Definition

Each wave includes:

- a single `category` (from the feature requirement list)
- a single `touches` type: `transaction_boundary | concurrency | replay | none`
- explicit entry/exit criteria

## Waves

### W0: Quality Gate Stabilization (CI + policy)

- **Module group**: CI/static-analysis wiring
- **Category**: naming_cleanup (doc/config normalization only)
- **Touches**: none
- **Purpose**: Make Sonar/Qodana runs deterministic and enforceable before code refactors.
- **Exit criteria**:
  - Sonar workflow runs analysis and reliably reports quality gate status.
  - Qodana workflow runs on both PR and push events without checkout errors.
  - Policy enforced: block new criticals; exception format established.

### W1: Ledger Foundation Long Methods

- **Module group**: `ledger` foundation
- **Category**: long_methods
- **Touches**: none
- **Purpose**: Reduce cognitive complexity without changing transaction boundaries.
- **Exit criteria**:
  - All existing ledger foundation tests pass.
  - No behavioral diff in posting outcomes, audit events, or idempotency.
  - If any touched class is transaction-critical: replay/concurrency validation per policy.

### W2: Ledger Foundation Duplication

- **Module group**: `ledger` foundation
- **Category**: duplication
- **Touches**: none
- **Purpose**: Remove duplication by extracting pure helpers and reducing repeated null/scale handling.
- **Exit criteria**: same as W1.

### W3: Balance Module Long Methods (no concurrency/replay changes)

- **Module group**: `ledger/balance`
- **Category**: long_methods
- **Touches**: none
- **Purpose**: Improve maintainability without changing protected-write, retry, or replay ordering behavior.
- **Exit criteria**:
  - Balance unit + integration tests pass.
  - Concurrency tests pass if they cover the touched flow.
  - Replay determinism validation passes for affected flows (mandatory).

### W4: Replay Determinism Hardening

- **Module group**: `ledger/balance` (replay-sensitive subset)
- **Category**: transaction_consistency
- **Touches**: replay
- **Purpose**: Tighten deterministic ordering and replay boundaries while keeping transaction boundaries unchanged.
- **Exit criteria**:
  - Deterministic replay validation shows identical outcomes vs baseline.
  - No new critical findings; any exception is time-bound and recorded.

### W5: Concurrency Safety (protected writes)

- **Module group**: `ledger/balance` (protected-write subset)
- **Category**: concurrency_safety
- **Touches**: concurrency
- **Purpose**: Address lock/retry contentions and concurrency-related findings without changing transaction boundaries.
- **Exit criteria**:
  - Concurrency integration tests pass and are non-flaky.
  - No overspend or inconsistent reservation outcomes in tests.

### W6: Transaction Boundary Refactors (only if required)

- **Module group**: `ledger` and/or `ledger/balance` (explicitly scoped)
- **Category**: transaction_consistency
- **Touches**: transaction_boundary
- **Purpose**: Adjust transactional scopes only when a finding cannot be resolved safely otherwise.
- **Exit criteria**:
  - Targeted integration tests cover commit/rollback behavior.
  - Replay + concurrency validations pass where relevant.
  - Rollback plan confirmed (pure revert or guarded dual-path if needed).

## Rollback Guidance (baseline)

- Prefer rollback by reverting the wave commit(s).
- If a wave includes a schema change, it must be expand-compatible and have a
  roll-forward plan; avoid destructive migrations.
