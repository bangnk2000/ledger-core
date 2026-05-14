# Quickstart: Account Balance Management

## Prerequisites

- Java 21 available through the Gradle toolchain.
- Docker available for PostgreSQL integration testing because concurrency and
  replay verification use Testcontainers PostgreSQL.
- Repository root: `/home/bangnk/personal/ledger-core`.

## Inspect the Feature Plan

```bash
sed -n '1,240p' specs/002-account-balance-management/plan.md
sed -n '1,260p' specs/002-account-balance-management/data-model.md
sed -n '1,260p' specs/002-account-balance-management/contracts/balance-api.openapi.yaml
sed -n '1,260p' specs/002-account-balance-management/contracts/module-interaction-contracts.md
```

## Expected Implementation Shape

1. Add a `balance` bounded-context subtree under
   `src/main/java/com/bangnk/ledgercore/ledger_core/ledger/balance`.
2. Keep balance domain models free of Spring and JPA annotations.
3. Expose only explicit ports from ledger-foundation for immutable history,
   replay exports, and posting outcome references.
4. Implement protected write use cases for reserve, confirm, cancel, release,
   expire, rebuild, and reconcile with explicit transaction boundaries.
5. Keep live balance queries, reservations, rebuild checkpoints, and
   reconciliation records in balance-owned persistence adapters only.
6. Add Flyway migration
   `src/main/resources/db/migration/V2__create_balance_management.sql` as an
   additive zero-downtime change.
7. Add contract, integration, concurrency, and replay-determinism tests for the
   critical flows.

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
  --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web.GetCurrentBalanceContractTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web.ReserveFundsContractTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationConcurrencyIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationRecoveryIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReplayRebuildIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReconciliationIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.balance.domain.FundsReservationTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.balance.domain.BalanceStateTest
```

## Manual API Smoke Examples

Start the application:

```bash
GRADLE_USER_HOME=/tmp/gradle-home ./gradlew bootRun
```

Create a reservation:

```bash
curl -i -X POST http://localhost:8080/api/v1/balances/reservations \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: reserve-001' \
  -H 'X-Requester-Scope: internal-test' \
  -H 'X-Correlation-Id: corr-balance-001' \
  -d '{
    "accountId": "cash",
    "currency": "USD",
    "amount": "25.00",
    "direction": "DEBIT",
    "expiresAt": "2026-05-14T12:00:00Z",
    "businessReference": "payment-auth-1001",
    "actor": {
      "actorId": "balance-test",
      "actorType": "SYSTEM"
    }
  }'
```

Read the current balance:

```bash
curl -i "http://localhost:8080/api/v1/balances/accounts/cash/current?currency=USD&consistency=STRONG"
```

Start a rebuild:

```bash
curl -i -X POST http://localhost:8080/api/v1/balances/rebuild-jobs \
  -H 'Content-Type: application/json' \
  -d '{
    "scope": {
      "accountIds": ["cash"]
    },
    "replayContractVersion": "v1",
    "restartFromCheckpoint": true
  }'
```

## Rollout Notes

- `V2__create_balance_management.sql` must be additive only.
- Ledger-foundation tables remain unchanged except for explicitly documented
  additive exports or views.
- Rolling deployment must tolerate the balance schema existing before balance
  endpoints are used.
- Replay export versions must remain backward compatible for supported rebuild
  tooling.
- Protected writes must fail closed whenever balance-management cannot uphold
  its consistency guarantees.

## Remaining Verification Gaps

- Manual `curl` flows are documented here but were not executed in this
  planning turn because the application was not started interactively.
- The helper scripts in `.specify/scripts/bash/` currently have CRLF line
  endings in their shebangs, so the plan flow used direct file discovery rather
  than the generated setup helpers.
