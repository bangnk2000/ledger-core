#!/usr/bin/env bash

PROJECT_KEY="bangnk2000_ledger-core"

curl -s -u "${SONAR_TOKEN}:" \
"https://sonarcloud.io/api/issues/search?projects=${PROJECT_KEY}&ps=500" \
-o build/reports/sonar-report.json