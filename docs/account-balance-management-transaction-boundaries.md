# Account Balance Management Transaction Boundaries

## Scope

This document defines protected-write transaction boundaries for the balance
bounded context and expected rollback behavior for failure paths.

## Protected Write Operations

The following use cases execute inside explicit protected-write transactions:

- Reserve funds
- Confirm reservation
- Release or cancel reservation
- Expire reservation

Each operation acquires deterministic locks on affected balance state and
reservation rows before state transitions are applied.

## Transaction Boundary Rules

1. Begin transaction in application service via `BalanceTransactionPort`.
2. Acquire deterministic account/reservation locks.
3. Validate consistency guard, idempotency, and domain invariants.
4. Persist balance/reservation/idempotency changes atomically.
5. Commit only when all invariants hold.

No partial updates are allowed across balance state, reservation state, and
idempotency records.

## Retry and Contention

- Lock timeout and transient contention errors are retried through
  `ProtectedWriteRetryExecutor` using `BalanceRetryPolicy`.
- Retry attempts are bounded and observable.
- If retry policy is exhausted, operation returns a stable contention/failure
  outcome without committing partial state.

## Rollback Behavior

Any exception during validation or persistence rolls back the full protected
write:

- Balance state remains unchanged
- Reservation state remains unchanged
- No duplicate idempotency success record is written

Rollback is mandatory for:

- Insufficient funds violations
- Idempotency payload conflicts
- Lock timeout/deadlock after retry exhaustion
- Degraded-state fail-closed guard activation

## Failure Isolation

- Protected writes fail closed when consistency guarantees cannot be upheld.
- Rebuild/reconciliation failures do not mutate immutable ledger history.
- Balance-write rollback never alters posted ledger entries.
