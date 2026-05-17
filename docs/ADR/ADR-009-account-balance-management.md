# ADR-009 - Account Balance Management Bounded Context

## Status

Accepted

## Context

The platform requires protected reserve/confirm/release flows, reliable current
balance snapshots, and deterministic rebuild/reconciliation without mutating
immutable ledger history.

The existing ledger foundation remains authoritative for posted accounting
entries and must preserve immutability and double-entry correctness.

## Decision

Introduce `ledger.balance` as a bounded context inside the modular monolith
with explicit ports/adapters and contract-based integration with ledger
foundation.

Key constraints:

- Immutable ledger remains source of truth for finalized accounting history.
- Balance module owns derived current-state tables and reservations.
- Protected writes use explicit transaction boundaries, deterministic locking,
  bounded retries, and fail-closed behavior.
- Rebuild and reconciliation consume replay-safe, versioned ledger exports.
- All API and data evolution remains additive and zero-downtime compatible.

## Consequences

### Positive

- Preserves financial correctness and traceability.
- Isolates derived-state concerns from immutable ledger history.
- Supports deterministic recovery and reconciliation workflows.
- Keeps extraction path open for future service split without redesign.

### Negative

- Additional module complexity inside the monolith.
- Operational overhead for rebuild/reconciliation controls and observability.
- Requires strict contract discipline between ledger foundation and balance.

## Notes

This ADR aligns with constitution principles for hexagonal architecture,
transaction explicitness, backward compatibility, and modular-monolith-first
delivery.
