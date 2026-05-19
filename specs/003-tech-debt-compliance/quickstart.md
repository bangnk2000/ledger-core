# Quickstart: Working With Sonar + Qodana During Refactors

**Feature**: `003-tech-debt-compliance`  
**Date**: 2026-05-19  

This quickstart is optimized for incremental waves and correctness protection
in transaction-critical and replay-sensitive modules.

## Local Workflow

### 1) Baseline build + tests

Run the standard build and unit tests:

```bash
GRADLE_USER_HOME=/tmp/gradle-home ./gradlew build
```

For focused test runs, prefer `--tests` for the module being changed.

### 2) Sonar (SonarCloud)

The repo includes a helper script to fetch Sonar issues into a local report:

```bash
SONAR_TOKEN=... tools/sonar/fetch-sonar-report.sh
```

Notes:
- This writes `build/reports/sonar-report.json`.
- CI should run the real analysis task and enforce the quality gate (see
  `contracts/quality-gate-policy.md`).

### 3) Qodana

Qodana configuration lives in `qodana.yaml` and CI runs the scan via
`.github/workflows/qodana_code_quality.yml`.

Notes:
- For PRs, consider `pr-mode: true` to focus on changed files.
- For branch pushes (e.g., `develop`), use full scan to prevent drift.

## Correctness Checkpoints (transaction-critical / replay-sensitive)

Before merging a wave that touches any module classified as transaction-critical
or replay-sensitive (see `contracts/module-classification.md`):

1. Run the relevant regression + integration tests for the touched area.
2. Run the replay determinism check(s) used by the module (or add them as part
   of Wave 0/1 tasks if missing).
3. Run concurrency validation if the wave touches protected writes, locking, or
   retry behavior.

## Wave Rules

- One wave = one module group + one category.
- Do not mix changes that touch transaction boundaries with concurrency or
  replay refactors in the same wave.
- If a time-bound exception is approved for a new critical finding, the wave
  still requires mandatory replay/concurrency validation in transaction-critical
  modules.

