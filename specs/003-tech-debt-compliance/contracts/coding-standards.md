# Coding and Architecture Refactor Standards

**Feature**: `003-tech-debt-compliance`  
**Date**: 2026-05-19

These standards are mandatory for technical-debt refactor waves in this
feature. They preserve behavior and maintain ledger correctness guarantees.

## Hexagonal Dependency Rules

- Domain and application layers must not depend on adapter implementations.
- Inbound and outbound adapters may depend on application ports only.
- Refactors must reduce complexity without introducing cross-module shortcuts.
- Framework-specific concerns remain in adapters/configuration boundaries.

## Transaction-Boundary Rules

- Transaction boundaries must be explicit and intentionally scoped.
- Do not widen `@Transactional` scope as a side effect of readability refactors.
- Any transaction-boundary change must be isolated in a dedicated wave with
  commit/rollback integration evidence.
- Never mix transaction-boundary changes with concurrency or replay changes in
  the same wave.

## Replay Determinism Rules

- Replay order must be deterministic for identical input datasets.
- Refactors must not introduce implicit ordering based on non-deterministic
  collection iteration.
- Replay-sensitive logic must use explicit ordering/comparators where needed.
- Replay determinism validation is required for transaction-critical and
  replay-sensitive modules.

## Concurrency Safeguard Rules

- Preserve existing bounded retry and fail-closed behavior.
- Do not alter lock ordering semantics without dedicated characterization tests.
- Concurrency-related changes require deterministic, non-flaky assertions.
- Protected-write flows must continue to prevent overspend and inconsistent
  reservation state under contention.

## Refactor Hygiene Rules

- Prefer pure helper extraction over behavior rewrites.
- One wave must target one module group and one risk category.
- Every wave must include evidence links to executed validation commands.
- If a critical finding is deferred, use a time-bound exception record.
