#!/usr/bin/env bash
# End-to-end demo: create an incident, ingest logs, investigate, print the saved report.
# Usage: scripts/demo.sh [base-url]   (backend must be running, e.g. with the "demo" profile)
set -euo pipefail
API="${1:-http://localhost:8080}/api"
LOG_FILE="$(dirname "$0")/../backend/src/main/resources/samples/db-pool-exhaustion.log"

echo "1. Creating incident"
ID=$(curl -fsS -X POST "$API/incidents" -H 'Content-Type: application/json' -d '{
  "title":"Order API returning 503s","description":"Requests hang then fail",
  "service":"order-service","environment":"production","severity":"CRITICAL"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["id"])')
echo "   incident id=$ID"

echo "2. Ingesting logs"
python3 -c 'import json,sys;print(json.dumps({"logs":open(sys.argv[1]).read()}))' "$LOG_FILE" |
  curl -fsS -X POST "$API/incidents/$ID/logs" -H 'Content-Type: application/json' -d @- >/dev/null

echo "3. Investigating"
INV=$(curl -fsS -X POST "$API/incidents/$ID/investigate" | python3 -c 'import sys,json;print(json.load(sys.stdin)["id"])')

echo "4. Saved investigation report #$INV"
curl -fsS "$API/investigations/$INV" | python3 -m json.tool
