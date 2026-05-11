# Messaging Guidelines

Messaging and event-driven flows must be introduced only when they improve
scalability, isolation, reliability, or domain clarity.

## Adoption Rules

- Keep the modular monolith path first unless async processing has a documented
  need.
- Avoid distributed transactions whenever possible.
- Do not introduce event sourcing unless an ADR documents why simpler ledger
  entry modeling is insufficient.
- Every event or consumer design MUST identify its consistency model.

## Required Event Review

For events and messaging, define:

- Delivery guarantees.
- Retry strategy.
- Deduplication strategy.
- Ordering requirements.
- Failure handling and dead-letter behavior.
- Observability signals for publishing, consuming, retries, and failures.

## Idempotency and Deduplication

- Consumers MUST be idempotent when duplicate delivery is possible.
- Event handlers that mutate ledger state MUST preserve double-entry accounting
  and immutable ledger history.
- Deduplication keys MUST be durable and scoped to the event source and
  business operation.

## Ordering and Consistency

- Ordering requirements MUST be explicit per aggregate, account, ledger, or
  business process.
- Designs MUST document how out-of-order events are detected, delayed, retried,
  or rejected.
- Compensating actions MUST be documented for eventual consistency flows.
