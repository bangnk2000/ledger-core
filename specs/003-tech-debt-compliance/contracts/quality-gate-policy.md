# Quality Gate Policy: SonarQube/SonarCloud + Qodana

**Feature**: `003-tech-debt-compliance`  
**Date**: 2026-05-19  

This policy defines what blocks merges, how exceptions work, and what extra
validation is mandatory for transaction-critical/replay-sensitive modules.

## Policy Goals

- SonarQube/SonarCloud quality gate must pass for release-candidate branches.
- Qodana must report zero unresolved critical issues in scope.
- No new critical issues may be introduced; exceptions are time-bound and rare.
- Correctness takes precedence over "score improvements".

## Enforcement Rules

### Sonar (Quality Gate)

- CI must run Sonar analysis as part of PRs targeting `develop` and pushes to
  `develop`.
- CI must fail if Sonar quality gate fails.
- CI must fail if a PR introduces any new `critical` issue, unless covered by a
  time-bound exception record.
- PR new-code scope must be configured to compare against `develop` and must
  block new-code critical issues (policy baseline for this feature).

Implementation notes (to be executed as tasks):

- Prefer explicit analysis execution (do not rely on implicit build hooks).
- Prefer waiting for gate status where supported (e.g., `sonar.qualitygate.wait=true`).
- Ensure auth properties are passed to the analysis task (token + host url).
- Canonical invocation pattern: `./gradlew sonar -Dsonar.token=<SONAR_TOKEN> -Dsonar.host.url=<SONAR_HOST_URL> -Dsonar.qualitygate.wait=true`.
- Sonar-side gate thresholds should be tightened in stages:
  - Stage A (active): new `critical` issues = 0 on PR new code.
  - Stage B (pending verification): new `major` issues = 0 on PR new code.
  - Stage C (pending verification): tighten maintainability/reliability rating on PR new code.

### Qodana (Critical Findings)

- CI must run Qodana for PRs and default branches.
- CI must fail if any new `critical` finding is introduced, unless covered by a
  time-bound exception record.
- For PRs, the policy may use PR-diff mode to reduce noise, but the policy
  still blocks new critical findings in changed code.

Implementation notes (to be executed as tasks):

- Fix event-aware checkout so push builds do not reference PR-only context.
- Configure Qodana quality gate in `qodana.yaml` with
  `failureConditions.severityThresholds.critical: 0`.
- Run Qodana in baseline mode with
  `--baseline tools/qodana/qodana.sarif.json` so existing debt can be tracked
  while newly introduced critical findings are merge-blocking.

## Time-Bound Exceptions

Exceptions are allowed only to keep delivery moving when a critical issue is:

- a false positive that cannot be fixed safely within the wave, or
- a large fix that must be split into later waves due to risk.

### Exception Requirements (mandatory)

- Must be time-bound with an explicit expiration date.
- Must name the exact finding id(s), affected file(s), and rationale.
- Must define the "next wave" backlog item that removes the exception.
- Must include mandatory replay/concurrency validation if the change touches
  any transaction-critical or replay-sensitive module.

### Exception Recording

Record exceptions as an appended entry in this file under `## Exception Records`
so review and audit is centralized.

CI enforcement (Wave W0):

- The Qodana workflow validates this document contains the
  `## Exception Records` section.
- The workflow also performs UTC date validation for exception `expires_on`
  entries and fails if any dated entry is expired.

## Transaction-Critical Overrides

For any PR that touches modules classified in `contracts/module-classification.md`
as transaction-critical or replay-sensitive:

- Mandatory replay determinism validation is required before merge.
- Mandatory concurrency validation is required when protected writes, locking,
  or retry logic is touched.
- Time-bound exceptions do not waive the above validations.

## Must-Run Test Baseline (Transaction-Critical Override)

When a wave touches transaction-critical or replay-sensitive modules, run this
minimum test baseline before merge approval:

- `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReplayDeterminismIntegrationTest`
- `GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --tests com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.BalanceReservationConcurrencyIntegrationTest`

Additional requirements:

- If a wave touches transaction-boundary code, include targeted integration
  tests for commit/rollback behavior in the changed service scope.
- Evidence must be linked in wave records with exact command and timestamp.
- Failures in this baseline are merge-blocking even when Sonar/Qodana exceptions
  are approved.

## Wave Validation Checklist

Use this checklist for every wave record in `contracts/refactor-backlog.md`.

- Wave metadata:
  - Wave ID and date
  - backlog item IDs included in the wave
  - explicit `touches` classification (`none|replay|concurrency|transaction_boundary`)
- Required validation commands:
  - targeted tests for changed module behavior
  - if `touches: replay`: `BalanceReplayDeterminismIntegrationTest`
  - if `touches: concurrency`: `BalanceReservationConcurrencyIntegrationTest`
  - if `touches: transaction_boundary`: targeted commit/rollback integration tests
- Evidence format:
  - exact command line
  - execution environment (`sandbox` or `elevated rerun`)
  - UTC date
  - result (`BUILD SUCCESSFUL` or failure summary)
- Safety checks:
  - no API response shape change for externally exposed endpoints in scope
  - no cross-wave mixing of replay/concurrency/transaction-boundary changes
  - rollback note captured for the wave
- Required evidence links:
  - code/test file paths touched by the wave
  - section link in `contracts/refactor-backlog.md` containing results

## Canonical Wave Record Schema (T053)

Use this field set for Phase 6+ wave/task evidence records across contracts
docs. Historical W1-W5 records are not retroactively required to include every
field, but backfill to this schema is recommended when those records are
touched.

- `wave_id`: `W0`..`Wn` (or `NA` for non-wave task records like T006/T007/T008)
- `backlog_item_id`: backlog identifier (for example `LEDGER-TX-001`, `SONAR-CRIT-20260519-001`)
- `status`: `DONE | DONE_WITH_CONCERNS | BLOCKED`
- `scope`: concise statement of affected area and change intent
- `evidence`: command/artifact/test links with UTC date where applicable
- `decision`: explicit outcome (implemented, deferred, or no-op) with rationale
- `rollback_notes`: rollback guidance or `not_applicable`
- `pending_verification`: explicit statement when tool-side verification is not available

## Exception Records

*(Empty by default; add entries only when an exception is explicitly approved.)*

Format:

- `id`: EXC-YYYYMMDD-###
- `tool`: sonar|qodana
- `finding_id`: <tool finding id>
- `scope`: <paths>
- `rationale`: <why safe fix is deferred>
- `expires_on`: YYYY-MM-DD
- `required_validations`: <replay/concurrency/tests>
- `approver`: <role/person>

Expiry handling rules:

- Expired exceptions are invalid and must be removed or renewed before merge.
- Renewal must include a new explicit `expires_on` date and rationale update.
- Exception IDs must remain stable for audit traceability.

## Baseline Snapshot Decision Log (T006/T007)

- `date`: 2026-05-19
- `task`: T006 (Sonar snapshot pull)
- `command`: `node -e "const fs=require('fs'); const j=JSON.parse(fs.readFileSync('build/reports/sonar-report.json','utf8')); const critical=j.issues.filter(i=>i.severity==='CRITICAL'); const major=j.issues.filter(i=>i.severity==='MAJOR'); console.log('total='+j.total); console.log('critical_count='+critical.length); console.log('major_count='+major.length);"`
- `result`: `DONE`
- `evidence`: `build/reports/sonar-report.json` exists and parsed as `total=50`, `critical_count=2`, `major_count=48`.
- `artifact provenance`: snapshot was made available locally from `tools/sonar/build/reports/pr-2.json` as stated in task context.
- `decision`: treat this Sonar snapshot as current triage input for T008/T009.

- `date`: 2026-05-19
- `task`: T007 (Qodana snapshot + baseline decision)
- `artifact check`: `node -e "const fs=require('fs'); const j=JSON.parse(fs.readFileSync('tools/qodana/qodana.sarif.json','utf8')); console.log('qodana_runs='+((j.runs||[]).length));"` reports `qodana_runs=0`
- `result`: `BLOCKED` (snapshot pull missing)
- `baseline path decision`: baseline path remains
  `tools/qodana/qodana.sarif.json`; source-of-truth baseline content must come
  from CI artifact or local scan SARIF and replace placeholder in a dedicated
  change.

## Phase 6 Gate Tightening Status (T054/T055)

- `date`: 2026-05-24
- `task`: T054 (Qodana gradual thresholds)
- `wave_id`: `W0`
- `status`: `DONE_WITH_CONCERNS`
- `decision`: `qodana.yaml` enforces `critical: 0` with conservative active caps (`high: 25`, `moderate: 120`) to tighten gradually.
- `pending_verification`: T007 remains blocked (`qodana_runs=0` placeholder baseline), so high/moderate tightening cannot be safely enforced yet.

- `date`: 2026-05-24
- `task`: T055 (Sonar gate tightening + PR new-code alignment)
- `wave_id`: `W0`
- `status`: `BLOCKED`
- `decision`: PR new-code target policy is documented, but enforcement tightening on Sonar server-side quality gate is pending external configuration verification.
- `pending_verification`: Sonar server-side gate/project settings are external to this repository and were not verifiable in this session.
