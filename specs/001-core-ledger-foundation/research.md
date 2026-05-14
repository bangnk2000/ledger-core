# Research: Core Ledger Foundation

## Decision: Use the existing Java 21 and Spring Boot 4 stack

**Rationale**: The repository already declares Java 21 and Spring Boot 4.0.6 with Web MVC, JPA, validation, Flyway, Actuator, PostgreSQL, and test starters. The constitution names Java Spring Boot as the default application framework and PostgreSQL as the system of record.

**Alternatives considered**: Introducing Go for core ledger logic was rejected because ADR-008 limits Go to future worker-oriented infrastructure services. Introducing a separate service was rejected because ADR-001 selects a modular monolith first.

## Decision: Implement the ledger foundation as a modular-monolith ledger module

**Rationale**: ADR-001 requires one deployable application with explicit module boundaries while domain understanding evolves. A `ledger` module gives the core accounting model a clear home without introducing deployment or network complexity.

**Alternatives considered**: A microservice was rejected because it would introduce distributed transaction, deployment, and network failure concerns before the transaction boundaries are stabilized. A flat CRUD package layout was rejected because it would weaken domain invariants and traceability.

## Decision: Use hexagonal architecture inside the ledger module

**Rationale**: ADR-002 and the constitution require domain and application layers to remain independent from Spring, JPA, HTTP, PostgreSQL, queues, and external SDKs. Ports keep posting and balance use cases testable while adapters handle web and persistence concerns.

**Alternatives considered**: Direct controller-to-repository implementation was rejected because it would bury double-entry validation, idempotency, transaction boundaries, and audit behavior in framework code.

## Decision: Model ledger posting with tactical DDD aggregates and value objects

**Rationale**: The feature has strong invariants: at least one debit and credit, exact debit/credit equality, immutable entries, correction by new postings, idempotent request outcomes, and audit traceability. Tactical DDD keeps these rules in domain/application code rather than database or controller-only checks.

**Alternatives considered**: Generic transaction and entry records without domain behavior were rejected because they increase the risk that callers or adapters bypass financial invariants.

## Decision: Use PostgreSQL transactions for atomic posting and idempotency

**Rationale**: ADR-005 selects PostgreSQL for ACID transactions, MVCC, row-level locking, and auditability. Posting must persist the transaction, entries, and idempotency outcome atomically so failures cannot expose partial records.

**Alternatives considered**: Application-level deduplication without a database uniqueness constraint was rejected because concurrent retries can race. Distributed transactions were rejected because the feature has one database and no external state mutation.

## Decision: Store idempotency records in the same transaction as ledger postings

**Rationale**: Mutating requests require stable request identifiers and durable duplicate handling. The idempotency record must include caller scope, request identifier, request hash, outcome category, and accepted transaction reference when applicable.

**Alternatives considered**: Stateless request hashes were rejected because they cannot return a durable stable outcome after process restarts. A cache-only deduplication strategy was rejected because it is not a reliable financial control.

## Decision: Use Flyway create-only initial migrations with zero-downtime discipline

**Rationale**: ADR-006 and the constitution require versioned PostgreSQL migrations and zero-downtime compatibility. This feature introduces new tables, indexes, and constraints without changing existing consumer behavior, so the migration can be additive.

**Alternatives considered**: Hibernate auto-DDL was rejected because it is not auditable or deterministic enough for financial schema evolution. Destructive migrations are unnecessary and violate migration rules.

## Decision: Expose additive HTTP APIs for posting and balance retrieval

**Rationale**: The current application already includes Spring Web MVC. Additive endpoints preserve backward compatibility and give integration tests a production-equivalent boundary. Error contracts distinguish accepted, rejected, duplicate, conflict, and failed outcomes.

**Alternatives considered**: A command-line or messaging-only contract was rejected because the current feature needs a clear synchronous retry-safe contract and no Kafka dependency. Kafka remains proposed for later asynchronous workflows after idempotency and outbox foundations exist.

## Decision: Calculate balances from posted entries for this foundation

**Rationale**: The specification requires ledger entries as the source of truth and balance derivation from posted entries. Querying committed posted entries avoids mutable balance history and keeps correction behavior explainable.

**Alternatives considered**: Maintaining mutable account balance rows was rejected for the foundation because it introduces extra mutation paths and reconciliation complexity. A later read model can be justified if performance requirements outgrow direct derivation.

## Decision: Use integration tests with PostgreSQL behavior

**Rationale**: Critical flows include atomic rollback, uniqueness constraints, concurrency, and balance derivation. These require production-equivalent persistence behavior beyond unit tests. Testcontainers is the likely test-only dependency for PostgreSQL integration tests.

**Alternatives considered**: H2-only testing was rejected because it does not fully represent PostgreSQL locking, isolation, numeric, and constraint behavior. Pure unit tests remain useful for domain invariants but are insufficient for the feature gate.
