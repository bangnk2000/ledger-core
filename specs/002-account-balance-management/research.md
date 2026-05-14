# Research: Account Balance Management

## Decision: Keep balance-management inside the modular monolith as a bounded context

**Rationale**: ADR-001 requires a modular monolith first, and the constitution
requires simplicity over premature distribution. The feature needs strong
consistency for protected writes, so keeping ledger-foundation and
balance-management in one deployable application avoids network and distributed
transaction failure modes while still allowing explicit module boundaries.

**Alternatives considered**: Immediate service extraction was rejected because
it would force cross-service consistency, deployment coupling, and replay/event
infrastructure before the balance workflows are stabilized.

## Decision: Use projection-based protected writes with immutable ledger authority

**Rationale**: The clarified spec requires ledger-foundation to remain the
authority for finalized ledger balance while balance-management owns current
state, reservations, rebuilds, and reconciliation. Projection-based protected
writes let balance-management protect spendable funds in its own write boundary
without turning derived state into the final source of accounting truth.

**Alternatives considered**: Pure event-driven eventual consistency was
rejected because protected spend and reservation flows must fail closed instead
of allowing overspend during lag. A single shared mutable balance model was
rejected because it would blur authority boundaries between ledger history and
derived operational state.

## Decision: Enforce contract-only dependency direction with controlled replay exports

**Rationale**: The balance bounded context must evolve independently and remain
extraction-ready. Contract-only integration prevents direct table coupling and
lets ledger-foundation change internal persistence details without breaking
balance-management, as long as immutable contracts and replay exports remain
compatible.

**Alternatives considered**: Direct reads of ledger tables for live operations
or rebuilds were rejected because they create hidden transactional assumptions,
schema lockstep, and tight coupling to persistence models. Shared mutable state
was rejected because it undermines bounded-context ownership.

## Decision: Introduce a dedicated `ledger/balance` source subtree

**Rationale**: The repository already contains the foundational `ledger`
bounded context under `src/main/java/com/bangnk/ledgercore/ledger_core/ledger`.
Adding a sibling `balance` subtree under the same module root makes the
separation explicit without forcing a large refactor of the existing
ledger-foundation code.

**Alternatives considered**: Reusing the existing foundation packages for all
balance classes was rejected because it would weaken the ownership boundary.
Creating a separate Gradle module was rejected because the repository is still
intentionally a modular monolith with one application artifact.

## Decision: Use explicit protected-write transactions with pessimistic locking and version checks

**Rationale**: Spend validation, reservation creation, confirmation, release,
and multi-account transfer protection all require deterministic behavior under
contention. PostgreSQL row-level locking plus explicit account ordering and a
monotonic balance-state version provide a pragmatic mix of safety and conflict
detection within one database transaction boundary.

**Alternatives considered**: Optimistic-only concurrency was rejected for core
withdrawal and transfer protection because stale reads can allow overspend under
contention. Application-level locks without database enforcement were rejected
because they are not durable enough for financial correctness.

## Decision: Allow protected-write isolation instead of whole-system availability coupling

**Rationale**: The clarified failure model allows unrelated ledger postings to
continue when balance projections, rebuilds, or reconciliation are degraded,
while any flow that depends on available-funds protection must fail closed. This
preserves ledger authority and operational isolation without sacrificing funds
safety.

**Alternatives considered**: Blocking all ledger postings during any
balance-management failure was rejected because it couples availability too
tightly. Letting protected spend flows proceed during degradation was rejected
because it risks unsafe balance decisions and orphaned state.

## Decision: Use bounded event ownership

**Rationale**: Ledger-foundation should emit immutable accounting events and
replay/export checkpoints, while balance-management should emit derived balance,
reservation, rebuild, reconciliation, and recovery events. This aligns event
ownership with authoritative state ownership and keeps downstream consumers from
confusing projection outputs with immutable accounting history.

**Alternatives considered**: A single unified event stream owned by one module
was rejected because it would either leak balance internals into ledger
authority or make balance-management reinterpret ledger history as its own.

## Decision: Make all cross-module contracts additive and replay-safe

**Rationale**: The constitution requires backward compatibility and zero-downtime
evolution. Replay exports, immutable event schemas, and command/query contracts
must therefore be versioned additively with explicit compatibility windows so
either bounded context can change internally without breaking supported
consumers or rebuild tooling.

**Alternatives considered**: Lockstep internal contract changes were rejected
because they undermine independent evolution. Schema-sharing as the primary
integration mechanism was rejected because it prevents storage refactors and
future extraction.

## Decision: Keep schema ownership separate by bounded context

**Rationale**: Ledger-foundation owns immutable journal tables and posting
idempotency history. Balance-management should own `balance_state`,
`funds_reservations`, `balance_snapshots`, `balance_rebuild_jobs`,
`balance_rebuild_checkpoints`, and `balance_reconciliation_records`. This makes
migration boundaries explicit and supports isolated operational ownership.

**Alternatives considered**: Storing reservations or snapshots directly in
ledger tables was rejected because it would mix mutable operational state with
immutable accounting authority. Using transient-only balance state was rejected
because it weakens recovery and replay coordination.

## Decision: Expose explicit synchronous APIs plus controlled internal replay contracts

**Rationale**: The system already uses Spring Web MVC and HTTP contract tests.
Balance reads, reservation commands, confirmation/release operations,
reconciliation triggers, and rebuild triggers should be exposed through explicit
contracts, while ledger export and replay consumption remain internal
contract-first integration surfaces.

**Alternatives considered**: Message-only interfaces were rejected because the
feature does not yet require broker infrastructure. Hidden adapter-only
interfaces were rejected because they are harder to review for compatibility and
operational semantics.

## Decision: Verify the feature with PostgreSQL-backed integration and replay tests

**Rationale**: The hardest risks are lock behavior, idempotent retries, replay
determinism, failure isolation, and zero-downtime migration compatibility.
These must be tested against production-equivalent PostgreSQL behavior and HTTP
contracts rather than only unit tests.

**Alternatives considered**: In-memory databases were rejected because they do
not faithfully model PostgreSQL locking and transaction behavior. Manual-only
verification was rejected because the feature requires repeatable safety checks.
