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

## Ledger Posting Contract

- `POST /api/v1/ledger/postings` requires `Idempotency-Key` and
  `X-Requester-Scope` headers for every mutating request.
- `X-Correlation-Id` is optional but should be forwarded whenever available so
  posting outcomes and audit events can be traced across services.
- Accepted postings return `201 Created`.
- Stable duplicates return `200 OK` with the original committed outcome and
  transaction identifier.
- Conflicting retries with the same request identity but different payload hash
  return `409 Conflict`.
- Domain rejections such as unbalanced entries return `400 Bad Request` with a
  stable ledger error code.

## Backward Compatibility for Ledger APIs

- Adding response fields such as safe trace metadata is acceptable only when
  existing fields keep their semantics and clients may ignore the additions.
- Existing ledger error codes, outcome names, and header requirements must stay
  stable once clients depend on them.
- New optional request fields are preferred over changing validation semantics
  for existing required fields.
- Balance responses must continue to derive from committed posted entries only;
  compatibility must not be achieved by introducing mutable balance snapshots
  that change existing semantics.

## Error Contracts

- API designs and reviews MUST include error contract analysis.
- Errors MUST use stable, documented codes for client-handled cases.
- Validation errors MUST distinguish client input problems from domain rule
  violations and concurrency conflicts.
- Error responses MUST NOT expose secrets, internal SQL, stack traces, or
  infrastructure details.

## Ledger Error Guidance

- Rejected posting responses may include safe trace fields
  (`correlationId`, `causationId`, `actorId`, `actorType`) so callers can
  reconcile failures without exposing internals.
- Failed posting responses and audit events must use sanitized messages and
  stable codes such as `LEDGER_POSTING_FAILED`.
- Conflict responses should explain that the request identity was reused with a
  different payload, without echoing sensitive request bodies.

## Transaction Boundaries

- API handlers MUST delegate business behavior to application use cases.
- Application use cases MUST define explicit transaction boundaries.
- Ledger-mutating APIs MUST document the consistency model, double-entry
  validation, audit trail, concurrency behavior, and rollback strategy.
