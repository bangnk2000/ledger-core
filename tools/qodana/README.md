# Qodana Baseline

This folder stores the tracked Qodana baseline used to block new critical
findings while allowing known debt to be managed in planned waves.

## Baseline File

- `tools/qodana/qodana.sarif.json`
- Current status: placeholder scaffold only (empty SARIF structure).
- Real baseline source-of-truth is established in task `T007` after pulling an
  actual Qodana snapshot (CI artifact or local scan output).

## Safe Update Procedure

1. Run a full Qodana scan against the intended reference branch.
2. Export SARIF output and replace `tools/qodana/qodana.sarif.json` in one
   dedicated change.
3. Record why the baseline changed and link the evidence in
   `specs/003-tech-debt-compliance/contracts/refactor-backlog.md`.
4. Never update the baseline in the same change set as production code refactors.
5. Re-run policy checks to confirm "new criticals" still fail for introduced issues.

## CI Enforcement

- CI uses baseline comparison with:
  `--baseline tools/qodana/qodana.sarif.json`
- CI enforces critical-only quality gating in `qodana.yaml` via:
  `failureConditions.severityThresholds.critical: 0`
- Combined behavior:
  baseline mode tracks existing debt; quality gate blocks newly introduced
  critical findings.
- CI also validates time-bound exception expiry dates in UTC from
  `specs/003-tech-debt-compliance/contracts/quality-gate-policy.md`.

## Baseline Update Governance

1. Update baseline only in a dedicated PR with no application runtime/API code
   changes.
2. Include a short justification in the PR description and add evidence in
   `specs/003-tech-debt-compliance/contracts/refactor-backlog.md`.
3. If baseline expansion is needed for temporary exceptions, add/refresh the
   corresponding time-bound exception record in
   `specs/003-tech-debt-compliance/contracts/quality-gate-policy.md`.

## Local Example

```bash
qodana scan --save-report --baseline tools/qodana/qodana.sarif.json
```
