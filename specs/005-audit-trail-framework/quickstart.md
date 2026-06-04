# Quickstart: Audit Trail Framework

## Prerequisites

- Java 21 available through the Gradle toolchain.
- Docker available for PostgreSQL integration testing because persistence and
  query verification use Testcontainers PostgreSQL.
- Repository root: `/home/bangnk/personal/ledger-core`.

## Inspect the Feature Plan

```bash
sed -n '1,260p' specs/005-audit-trail-framework/plan.md
sed -n '1,260p' specs/005-audit-trail-framework/research.md
sed -n '1,260p' specs/005-audit-trail-framework/data-model.md
sed -n '1,260p' specs/005-audit-trail-framework/contracts/audit-framework-contracts.md
sed -n '1,260p' specs/005-audit-trail-framework/contracts/consumer-onboarding-contract.md
```

## Expected Implementation Shape

1. Add an `audit` bounded-context subtree under
   `src/main/java/com/bangnk/ledgercore/ledger_core/audit`.
2. Keep the audit domain model free of Spring, JPA, HTTP, and messaging
   annotations.
3. Expose application ports for capture, investigation query, publication
   backlog handling, retention processing, and integrity verification.
4. Add a Flyway migration
   `src/main/resources/db/migration/V4__create_audit_trail_framework.sql` as
   an additive zero-downtime change.
5. Preserve current ledger structured audit logging during phased rollout until
   characterization tests show that the shared framework provides equivalent or
   better investigation value.
6. Normalize actor, trace, ledger-transaction, and idempotency linkage through
   audit translators instead of exposing consumer persistence entities.
7. Ensure durable capture succeeds even when downstream publication is deferred.
8. Emit structured observability for capture, publication deferment, retention
   transitions, query access, and integrity verification outcomes.

## Run Verification

```bash
GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test
```

If the local sandbox blocks Testcontainers networking, rerun the same command
outside the sandbox with the same `GRADLE_USER_HOME` override.

## Focused Verification Targets

Planned focused suites for this feature:

```bash
GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test \
  --tests com.bangnk.ledgercore.ledger_core.audit.domain.AuditEventTest \
  --tests com.bangnk.ledgercore.ledger_core.audit.application.AuditCaptureServiceTest \
  --tests com.bangnk.ledgercore.ledger_core.audit.adapter.AuditPersistenceIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.audit.adapter.AuditPublicationIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.audit.adapter.AuditRetentionIntegrationTest \
  --tests com.bangnk.ledgercore.ledger_core.ledger.adapter.AuditFrameworkLedgerCharacterizationTest \
  --tests com.bangnk.ledgercore.ledger_core.balance.adapter.AuditFrameworkBalanceCharacterizationTest
```

## Consumer Adoption Example

Onboarded write flow:

1. Consumer use case completes its domain validation and business state change.
2. Consumer builds a shared audit capture request from local actor, trace,
   subject, ledger, and idempotency context.
3. Audit framework stores the immutable event durably.
4. Audit framework records publication backlog state for downstream delivery.
5. Authorized investigators query by correlation identifier, ledger
   transaction reference, or idempotency linkage as needed.

## Rollout Notes

- `V4__create_audit_trail_framework.sql` must be additive only.
- Existing ledger structured audit logs remain valid during the first rollout.
- Current module APIs stay backward compatible; onboarding is internal unless an
  additive query endpoint is introduced later.
- Consumer onboarding should proceed one workflow at a time with
  characterization coverage.
- Retention policy should start with shared active-retention and
  restricted-retention semantics, with stricter durations by event class where
  required.

## Verification Results

- Planning artifacts created on `2026-06-04`.
- Test execution has not been run as part of `/speckit-plan`; verification is
  deferred to implementation and review.

## Environment Notes

- `build.gradle` defines Java 21, Spring Boot 4.0.6, Flyway, JPA, Actuator,
  validation, Web MVC, PostgreSQL, and Testcontainers as the relevant baseline
  stack for this feature.
