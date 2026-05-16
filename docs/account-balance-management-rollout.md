# Account Balance Management Rollout and Recovery

## Zero-Downtime Rollout

Rollout uses additive schema and backward-compatible code deployment.

## Migration Sequence

1. Apply Flyway migration `V2__create_balance_management.sql` (additive only).
2. Deploy application version that can run with migration present.
3. Enable and validate balance endpoints/flows.
4. Monitor contention, retries, and fail-closed signals.

No migration step should require service downtime.

## Expand/Contract Discipline

- Expand phase: add new tables/indexes/fields only.
- Contract phase (if needed) occurs in a later release after compatibility
  window closes.
- Do not remove or repurpose fields in the same release that introduces them.

## Roll-Forward Recovery

If rollout issues occur, prefer roll-forward:

1. Keep additive schema in place.
2. Deploy patched application behavior.
3. Replay or rebuild derived balance state using rebuild tooling when needed.
4. Reconcile and record discrepancies for audit and operations.

## Rollback Expectations

- Application binaries can be rolled back only if they remain compatible with
  additive schema.
- Database rollback is not the default strategy for additive migrations.
- Protected writes fail closed while consistency is uncertain.

## Operational Validation

Post-deploy validation:

- Contract tests pass for balance endpoints.
- Domain and integration tests pass in CI.
- Quickstart verification commands run successfully.
- Observability shows no sustained contention or fail-closed anomalies.
