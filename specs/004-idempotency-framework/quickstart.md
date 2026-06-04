# Quickstart: Idempotency Framework

## Prerequisites

- Java 21 available through the Gradle toolchain.
- Docker available for PostgreSQL integration testing because duplicate and
  concurrency verification use Testcontainers PostgreSQL.
- Repository root: `/home/bangnk/personal/ledger-core`.

## Inspect the Feature Plan

```bash
sed -n '1,240p' specs/004-idempotency-framework/plan.md
sed -n '1,260p' specs/004-idempotency-framework/data-model.md
sed -n '1,260p' specs/004-idempotency-framework/contracts/idempotency-module-contracts.md
sed -n '1,260p' specs/004-idempotency-framework/contracts/consumer-onboarding-contract.md
```

## Expected Implementation Shape

1. Add an `idempotency` bounded-context subtree under
   `src/main/java/com/bangnk/ledgercore/ledger_core/idempotency`.
2. Keep the idempotency domain model free of Spring, JPA, HTTP, and queue
   annotations.
3. Expose application ports for claim, finalize, replay inspection, and
   cleanup, with PostgreSQL as the first persistence adapter.
4. Canonicalize only normalized business-material request fields for
   fingerprinting; exclude transport metadata such as headers, trace IDs, and
   adapter-specific timestamps.
5. Add a Flyway migration
   `src/main/resources/db/migration/V3__create_idempotency_framework.sql` as an
   additive zero-downtime change.
6. Preserve existing ledger and balance idempotency behavior until
   characterization tests prove consumer parity for any migrated flow.
7. Add focused consumer adapters or translators rather than letting consumer
   modules share raw persistence entities.
8. Emit structured observability for duplicate claims, conflicts, in-progress
   duplicates, replay-window expiry, tombstone retention, and indeterminate
   outcomes.

## Run Verification

```bash
GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test
```

If the local sandbox blocks Testcontainers networking, rerun the same command
outside the sandbox with the same `GRADLE_USER_HOME` override.

## Focused Verification Targets

The implementation should add and pass focused tests similar to these:

```bash
GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test \
  --tests com.bangnk.ledgercore.ledger_core.idempotency.domain.IdempotencyRecordTest \
  --tests com.bangnk.ledgercore.ledger_core.idempotency.application.IdempotencyClaimServiceTest \
  --tests com.bangnk.ledgercore.ledger_core.idempotency.adapter.IdempotencyPersistenceIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.idempotency.adapter.IdempotencyConcurrencyIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.idempotency.adapter.IdempotencyExpirationIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.IdempotencyFrameworkLedgerCharacterizationTest \
  --tests com.bangnk.ledgercore.ledger_core.balance.adapter.IdempotencyFrameworkBalanceCharacterizationTest
```

## Consumer Adoption Example

Protected request flow:

1. Consumer adapter builds scope, key, canonical fingerprint, and correlation
   metadata.
2. Consumer use case calls the idempotency claim port.
3. If the result is `FIRST_EXECUTION`, the consumer performs business work.
4. Consumer finalizes the durable replay outcome through the idempotency
   finalize port.
5. If the result is `REPLAY`, `CONFLICT`, `DUPLICATE_IN_PROGRESS`,
   `EXPIRED_KEY`, or `INDETERMINATE`, the consumer returns the stable framework
   outcome without running duplicate side effects.

## Rollout Notes

- `V3__create_idempotency_framework.sql` must be additive only.
- Existing `ledger_idempotency_records` and `balance_idempotency_records`
  remain valid during the first rollout.
- Rolling deployment must tolerate the new shared idempotency schema existing
  before any consumer is switched to it.
- Consumer onboarding should proceed one operation family at a time with
  characterization coverage.
- Retention policy should default to replay window followed by tombstone
  window; consumers may tune durations but must not bypass the two-stage model.

## Remaining Verification Gaps

- No tests were executed during this planning turn.
- The Spec Kit shell helpers in `.specify/scripts/bash/` required temporary
  CRLF normalization to run in this environment; repository scripts were not
  modified as part of planning.
