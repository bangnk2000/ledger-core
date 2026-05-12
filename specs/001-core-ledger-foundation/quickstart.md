# Quickstart: Core Ledger Foundation

## Prerequisites

- Java 21 available through the Gradle toolchain.
- Docker available for PostgreSQL integration testing because the ledger
  integration and contract tests use Testcontainers PostgreSQL.
- Repository root: `/home/bangnk/personal/ledger-core`.

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
GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test
```

If the local sandbox blocks wildcard IP detection for Testcontainers, rerun the
same command outside the sandbox with the same `GRADLE_USER_HOME` override.
The full suite passed on 2026-05-12 when executed outside the sandbox in an
environment where Testcontainers networking was available.

## Focused Smoke Verification

```bash
GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test \
  --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web.PostLedgerTransactionContractTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web.PostLedgerTransactionIdempotencyContractTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web.PostLedgerTransactionTraceContractTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web.GetAccountBalanceContractTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.domain.LedgerTransactionTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.domain.LedgerEntryImmutabilityTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.application.IdempotencyServiceTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.application.GetAccountBalanceServiceTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.application.AuditEventPublisherTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.PostLedgerTransactionIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.LedgerEntryImmutabilityIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.LedgerCorrectionIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.LedgerPostingConcurrencyIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.GetAccountBalanceIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.LedgerTraceRetentionIntegrationTest
```

The focused smoke command also passed on 2026-05-12.

## Manual API Smoke Examples

Start the application:

```bash
GRADLE_USER_HOME=/tmp/gradle-home ./gradlew bootRun
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
    "metadata": {
      "source": "quickstart"
    },
    "causationId": "cause-001",
    "submittedAt": "2026-05-12T09:00:00Z",
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
cat > /tmp/balanced-posting.json <<'JSON'
{
  "businessReference": "invoice-1001",
  "description": "Record invoice settlement",
  "metadata": {
    "source": "quickstart"
  },
  "causationId": "cause-001",
  "submittedAt": "2026-05-12T09:00:00Z",
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
}
JSON

curl -i -X POST http://localhost:8080/api/v1/ledger/postings \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: request-001' \
  -H 'X-Requester-Scope: internal-test' \
  -H 'X-Correlation-Id: corr-001' \
  -d @/tmp/balanced-posting.json
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
- Preferred rollback strategy is application rollback plus schema retention; do
  not destructively remove ledger tables after they may contain history.

## Remaining Verification Gaps

- Manual `curl` smoke flows are documented here but were not executed in this
  automation turn because the application was not started interactively.
