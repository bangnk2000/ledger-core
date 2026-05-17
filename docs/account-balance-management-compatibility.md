# Account Balance Management Compatibility

## API Compatibility

Balance APIs are additive and backward compatible by default.

Compatibility requirements:

- Existing request fields and response fields keep semantics.
- New response/request fields are optional for existing clients.
- Existing error codes and meanings remain stable.
- Idempotency behavior stays stable for retries and duplicates.

## Contract Versioning

- Public and internal contracts use explicit versions.
- Breaking changes require a migration plan and compatibility window.
- Supported older consumers must be able to ignore additive fields safely.

## Replay Export Compatibility

Immutable ledger replay export contracts consumed by balance rebuild tooling
must preserve deterministic semantics across supported versions.

Rules:

- Export ordering semantics cannot change within a version.
- Added fields must not alter replay determinism for older consumers.
- Contract deprecations require documented overlap window and rollout order.

## Additive Evolution Checklist

Before releasing a balance-management contract update:

1. Confirm change is additive and ignorable by old consumers.
2. Confirm no required field was introduced for existing endpoints.
3. Confirm replay export ordering and checkpoint semantics remain stable.
4. Confirm tests cover old behavior plus additive fields.
