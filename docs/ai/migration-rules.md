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

## Ledger Foundation Rollout Strategy

- The foundation migration for the ledger module is additive only: create new
  `ledger_transactions`, `ledger_entries`, and `ledger_idempotency_records`
  tables plus supporting indexes, constraints, and immutability guardrails.
- New application code must tolerate the schema existing before traffic is
  routed to the ledger endpoints.
- Existing application versions must tolerate the new ledger tables being
  present but unused during rolling deployment.
- New columns added to ledger tables in later releases must remain nullable or
  have safe defaults until all application instances understand them.

## Rollback and Roll-Forward

- Prefer roll-forward for ledger schema defects after deployment because ledger
  history must remain auditable and additive.
- If a release must be rolled back at the application layer, keep the additive
  schema in place and return traffic to the previous compatible application
  version.
- Do not attempt destructive rollback of ledger history tables once posting
  traffic may have used them.
- If a migration fails before traffic uses the new schema, resolve by
  completing or superseding the migration with a follow-up Flyway script rather
  than editing an applied migration in place.

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
- Uniqueness and immutability constraints that protect idempotency and ledger
  history are preferred over application-only checks, but they must be added in
  a way that does not block normal traffic longer than acceptable.
