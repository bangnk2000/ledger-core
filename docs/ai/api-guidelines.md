# API Guidelines

API changes in ledger-core must preserve operational stability, auditability,
and client compatibility.

## Compatibility

- Public and internal APIs MUST remain backward compatible whenever possible.
- Breaking changes require an ADR or equivalent design note, a migration path,
  a compatibility window, and documented consumer impact.
- Additive changes are preferred over changing or removing existing fields,
  endpoints, enum values, or error codes.
- Response contracts MUST remain stable for existing consumers during rolling
  deployments.

## Idempotency

- External requests that can mutate state MUST define idempotency behavior.
- Idempotency keys MUST be scoped to the caller and operation.
- Duplicate requests MUST return the previously committed result or a stable
  conflict response without creating additional ledger effects.
- Idempotency storage must participate in the same explicit transaction
  boundary as the state mutation when correctness depends on it.

## Error Contracts

- API designs and reviews MUST include error contract analysis.
- Errors MUST use stable, documented codes for client-handled cases.
- Validation errors MUST distinguish client input problems from domain rule
  violations and concurrency conflicts.
- Error responses MUST NOT expose secrets, internal SQL, stack traces, or
  infrastructure details.

## Transaction Boundaries

- API handlers MUST delegate business behavior to application use cases.
- Application use cases MUST define explicit transaction boundaries.
- Ledger-mutating APIs MUST document the consistency model, double-entry
  validation, audit trail, concurrency behavior, and rollback strategy.
