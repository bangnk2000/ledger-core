# Sonar Report Fetch Helper

This folder provides a local helper script to pull Sonar issues into
`build/reports/sonar-report.json` for triage.

## Required Environment Variables

- `SONAR_TOKEN`: Sonar API token with permission to read issues.

## Optional Environment Variables

- `SONAR_HOST_URL`: Sonar base URL. Default: `https://sonarcloud.io`
- `SONAR_PROJECT_KEY`: Project key. Default: `bangnk2000_ledger-core`

## Usage

```bash
SONAR_TOKEN=... tools/sonar/fetch-sonar-report.sh
```

## Output

- `build/reports/sonar-report.json`
