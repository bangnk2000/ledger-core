# Review Checklist

Use this checklist for planning reviews, design reviews, pull requests, and
architecture reviews.

## Architecture

- detect architecture violations
- detect layering violations
- detect tight coupling between modules
- detect distributed monolith patterns
- detect unnecessary abstraction
- detect unjustified CQRS, event sourcing, or async processing
- detect missing ADRs for important decisions or principle deviations
- detect task-list execution that skipped required workflow stages

## Domain and Ledger Correctness

- detect bypassed domain invariants
- detect mutable ledger history
- detect missing double-entry validation
- detect balance mutations without traceability
- detect transaction flows without audit logging
- detect anemic domain model where behavior belongs in domain/application code

## Transactions and Concurrency

- detect transaction leaks
- detect implicit transaction boundaries
- detect missing idempotency for external mutating requests
- detect concurrency risks
- detect locking issues
- detect rollback strategy gaps

## Database

- detect unsafe migrations
- detect missing zero-downtime migration strategy
- detect missing rollback or roll-forward strategy
- detect missing index impact analysis
- detect long-running migration or backfill risks
- detect N+1 queries

## APIs

- detect backward compatibility breaks
- detect missing idempotency analysis
- detect undocumented error contracts
- detect response fields or codes that are unsafe for rolling deployments

## Events and Messaging

- detect missing delivery guarantees
- detect missing retry strategy
- detect missing deduplication strategy
- detect missing ordering requirements
- detect missing failure strategy or dead-letter behavior

## Operations, Security, and Observability

- detect observability gaps
- detect missing structured logging
- detect missing metrics or tracing
- detect missing authentication, authorization, input validation, or secrets
  handling analysis
- detect fake or placeholder implementations presented as production-ready
- detect missing code review evidence or missing finish-branch decision
