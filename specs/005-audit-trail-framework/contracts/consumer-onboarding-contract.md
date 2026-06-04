# Consumer Onboarding Contract: Audit Trail Framework

## Purpose

This contract defines how ledger, balance, and future modules adopt the shared
audit framework without bypassing existing bounded-context rules.

## Producer Responsibilities

Each consuming module must:

- declare which state changes are business-critical
- map local actor and trace data into the shared audit contract
- provide business subject identifiers that remain stable for investigation
- supply ledger transaction references when the action creates or depends on a
  ledger transaction
- supply idempotency key and durable idempotency record identifiers when the
  action is retry-protected and those identifiers exist
- provide only audit-safe detail payloads
- choose an allowed retention profile for the event category

## Framework Responsibilities

The audit framework must:

- normalize actor and trace values consistently across modules
- store accepted events immutably
- make missing optional values explicit
- preserve linkage to ledger and idempotency evidence where present
- make downstream publication operationally recoverable
- expose one investigation query model for all onboarded modules

## Onboarding Flow

1. Identify one business-critical workflow in the consumer module.
2. Characterize the current behavior and existing audit/logging outputs.
3. Add translation from local actor/trace/value objects into the shared audit
   capture request.
4. Capture audit events durably alongside the business workflow.
5. Verify linkage by business subject, correlation identifier, ledger
   transaction reference, and idempotency reference when applicable.
6. Preserve or phase down legacy module-local logging only after parity is
   proven.

## Ledger Module Mapping

Producer inputs likely come from:

- `AuditTrace`
- `LedgerTransaction`
- `RequestIdentity`
- posting outcome or correction outcome

Required linkage:

- ledger transaction reference for posted, rejected, reversed, or corrected
  transactions
- idempotency linkage when the posting flow used shared or legacy duplicate
  protection

## Balance Module Mapping

Producer inputs likely come from:

- `ActorContext`
- reservation, confirmation, release, rebuild, or reconciliation state
- request identity and correlation metadata
- optional ledger posting reference
- optional shared idempotency records

Required linkage:

- ledger transaction reference when balance flow depends on ledger posting
  output
- idempotency linkage for protected write flows

## Compatibility Rules

- Consumer modules must not depend on audit persistence entities directly.
- Existing public APIs remain unchanged unless an additive audit-facing field is
  explicitly approved.
- Legacy structured logs may coexist during migration but must not be treated
  as the only durable audit evidence once a workflow is onboarded.
- Consumer-specific fields must remain additive extensions over the shared
  minimum audit contract.

## Verification Expectations

- Contract tests confirm capture requests include required actor, trace, and
  business subject data.
- Integration tests confirm durable event storage and non-blocking publication
  behavior.
- Characterization tests confirm new framework capture preserves or improves
  existing investigation value for onboarded workflows.
- Query tests confirm ledger transaction and idempotency references are
  searchable when present.
