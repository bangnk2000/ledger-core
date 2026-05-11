# Database Migration Rules

PostgreSQL schema evolution must support zero-downtime deployment, rolling
updates, and auditable rollback or roll-forward.

## Required Analysis

Every database change review MUST include:

- Migration strategy.
- Rollback or roll-forward strategy.
- Zero-downtime compatibility analysis.
- Index impact analysis.
- Locking analysis.
- Data backfill and verification strategy when existing rows are affected.

## Zero-Downtime Rules

- Prefer expand-and-contract migrations for incompatible changes.
- Do not drop columns, constraints, indexes, or tables used by the currently
  deployed application version.
- Adding non-null constraints to populated tables requires a staged migration.
- Long-running backfills must be batchable, resumable, observable, and safe to
  run alongside normal traffic.
- Index creation on large tables must avoid blocking writes.

## Ledger Data Rules

- Ledger history MUST NOT be mutated to correct business mistakes.
- Corrections MUST be represented by new reversing or adjusting entries.
- Migrations MUST NOT bypass double-entry invariants.
- Balance-affecting data changes require an audit trail and reconciliation
  plan.

## Transaction and Locking Rules

- Migration scripts MUST avoid long transactions on hot tables.
- Lock acquisition risk must be identified before deployment.
- Application code and migrations must remain compatible during rolling
  updates.
