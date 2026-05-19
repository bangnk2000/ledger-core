# Research: Technical Debt Compliance (Sonar + Qodana)

**Feature**: `003-tech-debt-compliance`  
**Date**: 2026-05-19  

This research resolves planning-time unknowns for how static analysis is wired,
how to safely enforce quality gates, and how to keep refactors replay-safe and
transaction-safe.

## Decisions

### Decision 1: Treat transaction-critical/replay-sensitive modules as “hard gate”

**Chosen**: Any refactor touching transaction-critical or replay-sensitive
modules requires mandatory replay-determinism validation plus concurrency
validation (where concurrent writes/locks are relevant).

**Rationale**: These modules can silently change financial outcomes or replay
results even when static analysis “improves”.

**Alternatives considered**:
- Rely on unit tests only: rejected as insufficient for concurrency/replay risk.
- Allow exceptions without validation: rejected due to correctness risk.

### Decision 2: Fix CI wiring first, then refactor waves

**Chosen**: Wave 0 focuses on CI quality-gate correctness and report
repeatability before code refactors.

**Rationale**: If CI is misconfigured or flaky, refactor work will churn and
cannot be evaluated objectively.

**Alternatives considered**:
- Start with “easy smells” first: rejected because it risks masking CI issues.

### Decision 3: Use explicit, stable ordering for replay and “don’t trust defaults”

**Chosen**: For replay-related flows, use explicit ordering (already present in
`BalanceReplayOrderingService`) and avoid reliance on iteration order, implicit
sorting, database default ordering, or hash-based collections.

**Rationale**: Replay determinism depends on stable ordering across JVM/DB
versions and runtime conditions.

**Alternatives considered**:
- Assume stable iteration order: rejected (not a contractual guarantee).

## Findings (current repo wiring)

### Sonar (SonarCloud / SonarQube via Gradle)

- `build.gradle` uses `org.sonarqube` and defines `sonar { properties { ... } }`.
- `.github/workflows/sonarqube.yml` currently runs `./gradlew build` twice, and
  does not explicitly run the `sonar` task or pass token properties.
- `tools/sonar/fetch-sonar-report.sh` can fetch issues from SonarCloud API into
  `build/reports/sonar-report.json` if `SONAR_TOKEN` is available.

**Implication**: Quality gate failures may not be consistently attributable to
the same analysis run unless CI runs the intended analysis task with the
required auth properties.

### Qodana

- `qodana.yaml` uses `jetbrains/qodana-jvm-community:2026.1` and `projectJDK: 21`.
- `.github/workflows/qodana_code_quality.yml` checks out
  `${{ github.event.pull_request.head.sha }}` unconditionally; on `push` events
  that field is unset, so checkout can fail or behave unexpectedly.

**Implication**: Qodana CI stability and “new criticals” enforcement depends on
correct event-aware checkout and a defined failure condition policy.

## Best Practices Applied To This Feature

### Refactor safety in transaction-critical modules

- Prefer behavior-preserving refactors: rename, extract method, replace
  duplication with shared utility, simplify conditionals without changing
  outcomes.
- Avoid changing `@Transactional` boundaries in the same wave as concurrency or
  replay changes; isolate transaction-boundary changes into dedicated waves.
- Ensure “now” usage is injected (`Clock`) and not replaced with
  `Instant.now()` directly (except where already explicitly designed).

### Replay determinism

- Any list/stream processing that affects replay results must be explicitly
  sorted with a stable comparator (do not rely on database implicit ordering).
- Any rounding/scale decisions for money values must remain consistent (avoid
  changing scale defaults during “cleanup” refactors).

### Concurrency correctness

- Keep retry/backoff deterministic and bounded (already enforced via
  `BalanceRetryPolicy` + `ProtectedWriteRetryExecutor`).
- Validate lock/retry behavior with concurrency integration tests where
  protected writes exist.

## Open Items (resolved during tasks execution, not plan-time)

- Exact list of failing Sonar issues and Qodana findings must be pulled from
  CI artifacts or fetched from SonarCloud/Qodana reports and then mapped into
  `contracts/refactor-backlog.md` as concrete items with IDs and acceptance
  checks.

