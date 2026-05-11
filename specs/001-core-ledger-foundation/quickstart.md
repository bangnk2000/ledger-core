# Quickstart: Core Ledger Foundation

## Prerequisites

- Java 21 available through the Gradle toolchain.
- Docker available for PostgreSQL integration testing once Testcontainers is added.
- Repository root: `/home/bangnk/projects/ledger-core`.

## Inspect the Feature Plan

```bash
sed -n '1,220p' specs/001-core-ledger-foundation/plan.md
sed -n '1,220p' specs/001-core-ledger-foundation/data-model.md
sed -n '1,220p' specs/001-core-ledger-foundation/contracts/ledger-api.openapi.yaml
```

## Expected Implementation Shape

1. Add the `ledger` module under `src/main/java/com/bangnk/ledgercore/ledger_core/ledger`.
2. Keep domain models and value objects free of Spring and JPA annotations.
3. Put posting and balance use cases in the application layer with explicit transaction boundaries.
4. Put REST controllers in `adapter/in/web`.
5. Put JPA entities/repositories and SQL translation in `adapter/out/persistence`.
6. Add Flyway migration `src/main/resources/db/migration/V1__create_ledger_foundation.sql`.
7. Add integration tests for posting, idempotency, conflict detection, rollback, immutability, concurrency, and balance calculation.

## Run Verification

```bash
./gradlew test
```

After PostgreSQL integration tests are added, run the same command with Docker available so Testcontainers can start PostgreSQL.

## Manual API Smoke Examples

Start the application:

```bash
./gradlew bootRun
```

Submit a balanced posting:

```bash
curl -i -X POST http://localhost:8080/api/v1/ledger/postings \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: request-001' \
  -H 'X-Requester-Scope: internal-test' \
  -H 'X-Correlation-Id: corr-001' \
  -d '{
    "businessReference": "invoice-1001",
    "description": "Record invoice settlement",
    "actor": {
      "actorId": "system-ledger-test",
      "actorType": "SYSTEM"
    },
    "entries": [
      {
        "lineId": "debit-cash",
        "accountId": "cash",
        "direction": "DEBIT",
        "amount": "100.00",
        "currency": "USD"
      },
      {
        "lineId": "credit-revenue",
        "accountId": "revenue",
        "direction": "CREDIT",
        "amount": "100.00",
        "currency": "USD"
      }
    ]
  }'
```

Retry the same request and expect the same stable outcome without duplicate entries:

```bash
curl -i -X POST http://localhost:8080/api/v1/ledger/postings \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: request-001' \
  -H 'X-Requester-Scope: internal-test' \
  -H 'X-Correlation-Id: corr-001' \
  -d @balanced-posting.json
```

Request a balance:

```bash
curl -i http://localhost:8080/api/v1/ledger/accounts/cash/balance?currency=USD
```

## Rollout Notes

- Initial migration is additive and should create new ledger tables and indexes only.
- Do not drop or mutate existing structures during the foundation rollout.
- Rolling deployments must tolerate the schema existing before the application starts using it.
- Rejected and failed requests must not expose internal SQL, stack traces, secrets, or sensitive financial data.
