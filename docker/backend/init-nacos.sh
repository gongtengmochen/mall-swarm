#!/bin/sh
set -eu

NACOS_URL="${NACOS_URL:-http://nacos-registry:8848}"

echo "Waiting for Nacos at ${NACOS_URL}..."
until curl -fsS "${NACOS_URL}/nacos/v1/console/health/readiness" >/dev/null 2>&1 || \
      curl -fsS "${NACOS_URL}/nacos/v1/console/health/liveness" >/dev/null 2>&1; do
  sleep 5
done

echo "Importing prod configs into Nacos..."
for file in /configs/*/*-prod.yaml; do
  data_id="$(basename "${file}")"
  curl -fsS -X POST "${NACOS_URL}/nacos/v1/cs/configs" \
    --data-urlencode "dataId=${data_id}" \
    --data-urlencode "group=DEFAULT_GROUP" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content@${file}" >/dev/null
  echo "Imported ${data_id}"
done

echo "Nacos config import completed."
