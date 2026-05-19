# Quality Gate Policy: SonarQube/SonarCloud + Qodana

**Feature**: `003-tech-debt-compliance`  
**Date**: 2026-05-19  

This policy defines what blocks merges, how exceptions work, and what extra
validation is mandatory for transaction-critical/replay-sensitive modules.

## Policy Goals

- SonarQube/SonarCloud quality gate must pass for release-candidate branches.
- Qodana must report zero unresolved critical issues in scope.
- No new critical issues may be introduced; exceptions are time-bound and rare.
- Correctness takes precedence over “score improvements”.

## Enforcement Rules

### Sonar (Quality Gate)

- CI must run Sonar analysis as part of PRs targeting `develop` and pushes to
  `develop`.
- CI must fail if Sonar quality gate fails.
- CI must fail if a PR introduces any new `critical` issue, unless covered by a
  time-bound exception record.

Implementation notes (to be executed as tasks):

- Prefer explicit analysis execution (do not rely on implicit build hooks).
- Prefer waiting for gate status where supported (e.g., `sonar.qualitygate.wait=true`).
- Ensure auth properties are passed to the analysis task (token + host url).

### Qodana (Critical Findings)

- CI must run Qodana for PRs and default branches.
- CI must fail if any new `critical` finding is introduced, unless covered by a
  time-bound exception record.
- For PRs, the policy may use PR-diff mode to reduce noise, but the policy
  still blocks new critical findings in changed code.

Implementation notes (to be executed as tasks):

- Fix event-aware checkout so push builds do not reference PR-only context.
- Configure Qodana failure conditions to enforce `critical == 0` for the in-scope
  modules, with gradual tightening for lower severities as the backlog burns down.

## Time-Bound Exceptions

Exceptions are allowed only to keep delivery moving when a critical issue is:

- a false positive that cannot be fixed safely within the wave, or
- a large fix that must be split into later waves due to risk.

### Exception Requirements (mandatory)

- Must be time-bound with an explicit expiration date.
- Must name the exact finding id(s), affected file(s), and rationale.
- Must define the “next wave” backlog item that removes the exception.
- Must include mandatory replay/concurrency validation if the change touches
  any transaction-critical or replay-sensitive module.

### Exception Recording

Record exceptions as an appended entry in this file under `## Exception Records`
so review and audit is centralized.

## Transaction-Critical Overrides

For any PR that touches modules classified in `contracts/module-classification.md`
as transaction-critical or replay-sensitive:

- Mandatory replay determinism validation is required before merge.
- Mandatory concurrency validation is required when protected writes, locking,
  or retry logic is touched.
- Time-bound exceptions do not waive the above validations.

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

