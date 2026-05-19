# Risk Matrix: Technical Debt Refactors

**Feature**: `003-tech-debt-compliance`  
**Date**: 2026-05-19  

This matrix is used to size waves, choose validation, and decide rollback
triggers.

## Risk Entries

| Category | Likelihood | Impact | Detection Signals | Mitigation | Rollback Trigger |
|----------|------------|--------|-------------------|------------|------------------|
| duplication | medium | low | unit tests; diffs limited to extracted helpers | prefer pure extract/inline refactors; no behavior change; keep call sites identical | unexpected test failures or behavior diffs in integration tests |
| long_methods | medium | medium | tests; reduced cognitive complexity; unchanged outcomes | refactor via extract methods; keep ordering; avoid moving side effects | any change in posting/balance outcomes or audit events |
| large_classes | low | medium | tests; architectural checklists | split only when boundaries are clear; avoid new abstractions; keep public API stable | increased coupling or new dependency direction violations |
| naming_cleanup | low | low | compilation; tests | rename with IDE-safe refactors; avoid semantic changes | compilation failures or public API signature changes |
| transaction_consistency | medium | high | integration tests; transaction-boundary tests; DB state comparisons | isolate into dedicated waves; keep `@Transactional` changes small; document boundaries | any divergence in persisted state or inconsistent audit trail |
| concurrency_safety | medium | high | concurrency integration tests; lock contention metrics; flake rate | isolate into dedicated waves; bounded retries; deterministic lock order | deadlocks/timeouts/regressions in concurrency tests or observed contention spikes |
| logging_cleanup | medium | medium | log snapshot tests (if present); audit event structure checks | preserve event types/keys; no removal of required audit fields; keep metrics names stable | missing audit events, changed event types, or broken dashboards/alerts |
| query_optimization | low | high | integration tests; explain plans; p95 latency in perf tests | isolate to read-only waves first; add indices with zero-downtime migration; avoid semantic changes | changed result sets, missing rows, or latency regression |

## Special Notes For Ledger Correctness

- Any change touching transaction-critical or replay-sensitive modules must
  follow the mandatory validation rules in `contracts/module-classification.md`.
- Transaction-boundary, concurrency, and replay/determinism refactors are never
  mixed in the same wave.

