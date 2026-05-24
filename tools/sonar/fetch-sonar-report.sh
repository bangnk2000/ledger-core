#!/usr/bin/env bash
set -euo pipefail

PROJECT_KEY="${SONAR_PROJECT_KEY:-bangnk2000_ledger-core}"
SONAR_HOST_URL="${SONAR_HOST_URL:-https://sonarcloud.io}"
REPORT_DIR="build/reports"
OUTPUT_FILE="${REPORT_DIR}/sonar-report.json"

if [[ -z "${SONAR_TOKEN:-}" ]]; then
  echo "ERROR: SONAR_TOKEN is required" >&2
  exit 1
fi

mkdir -p "${REPORT_DIR}"

tmp_file="$(mktemp "${OUTPUT_FILE}.tmp.XXXXXX")"
cleanup() {
  rm -f "${tmp_file}"
}
trap cleanup EXIT

curl --fail --show-error --silent -u "${SONAR_TOKEN}:" \
  "${SONAR_HOST_URL}/api/issues/search?projects=${PROJECT_KEY}&ps=500" \
  -o "${tmp_file}"

if ! rg -q '"issues"[[:space:]]*:' "${tmp_file}" || ! rg -q '"paging"[[:space:]]*:' "${tmp_file}"; then
  echo "ERROR: Sonar response payload missing expected issues/paging fields" >&2
  exit 1
fi

mv "${tmp_file}" "${OUTPUT_FILE}"
echo "Saved Sonar issues report to ${OUTPUT_FILE}"
