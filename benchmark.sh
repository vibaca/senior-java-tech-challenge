#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://product-api:8080}"
HEALTH_ENDPOINT="$BASE_URL/actuator/health"
AUTH_USER="${AUTH_USER:-admin}"
AUTH_PASSWORD="${AUTH_PASSWORD:-password}"

TMP_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_DIR"' EXIT

calc_rate() {
  local success="$1"
  local duration="$2"
  if [ "$duration" = "0" ] || [ "$duration" = "0.0" ]; then
    echo "0"
    return
  fi
  echo "scale=2; $success / $duration" | bc
}

wait_for_api() {
  echo "Esperando a que la API esté lista en $HEALTH_ENDPOINT..."
  local start_ts end_ts
  start_ts=$(date +%s.%N)

  sleep 2
  until curl -sS "$HEALTH_ENDPOINT" | grep UP > /dev/null; do
    echo "Esperando API..."
    sleep 2
  done

  end_ts=$(date +%s.%N)
  APP_STARTUP_SECONDS=$(echo "$end_ts - $start_ts" | bc)
  echo "API lista. Tiempo de arranque observado: $APP_STARTUP_SECONDS s"
}

authenticate() {
  echo "Autenticando benchmark user..."
  local login_response
  login_response=$(curl -sS -f -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$AUTH_USER\",\"password\":\"$AUTH_PASSWORD\"}")

  TOKEN=$(echo "$login_response" | jq -r '.token // empty')
  if [ -z "$TOKEN" ]; then
    echo "Error: No se pudo obtener token JWT"
    echo "Response: $login_response"
    exit 1
  fi

  AUTH_HEADER=("-H" "Authorization: Bearer $TOKEN")
}

seed_data() {
  echo "Creating product..."
  local product_response
  product_response=$(curl -sS -f -X POST "$BASE_URL/products" \
    "${AUTH_HEADER[@]}" \
    -H "Content-Type: application/json" \
    -d '{"name":"Zapatillas deportivas","description":"Modelo 2025 edicion limitada"}')

  PRODUCT_ID=$(echo "$product_response" | jq -r '.productId // .id // empty')

  if [ -z "$PRODUCT_ID" ]; then
    echo "Error: Could not extract product ID from response"
    echo "Response: $product_response"
    exit 1
  fi

  echo "Product created with ID: $PRODUCT_ID"

  curl -sS -f -X POST "$BASE_URL/products/$PRODUCT_ID/prices" \
    "${AUTH_HEADER[@]}" \
    -H "Content-Type: application/json" \
    -d '{"value":99.99,"initDate":"2024-01-01","endDate":"2024-06-30"}' > /dev/null

  curl -sS -f -X POST "$BASE_URL/products/$PRODUCT_ID/prices" \
    "${AUTH_HEADER[@]}" \
    -H "Content-Type: application/json" \
    -d '{"value":129.99,"initDate":"2024-07-01","endDate":"2024-12-31"}' > /dev/null

  curl -sS -f -X POST "$BASE_URL/products/$PRODUCT_ID/prices" \
    "${AUTH_HEADER[@]}" \
    -H "Content-Type: application/json" \
    -d '{"value":199.99,"initDate":"2025-01-01","endDate":null}' > /dev/null
}

measure_avg_latency() {
  local name="$1"
  local url="$2"
  local samples="$3"
  local total="0"

  for _ in $(seq 1 "$samples"); do
    local t
    t=$(curl -sS -o /dev/null -w '%{time_total}' -f -X GET "$url" "${AUTH_HEADER[@]}")
    total=$(echo "$total + $t" | bc)
  done

  local avg
  avg=$(echo "scale=4; $total / $samples" | bc)
  echo "Latencia promedio [$name] (${samples} muestras): ${avg}s"
}

run_concurrent_test() {
  local name="$1"
  local total_requests="$2"
  local method="$3"
  local url="$4"
  local payload="${5:-}"

  local ok_file="$TMP_DIR/${name// /_}.ok"
  : > "$ok_file"

  local start_ts end_ts duration success rps
  start_ts=$(date +%s.%N)

  for i in $(seq 1 "$total_requests"); do
    if [ -n "$payload" ]; then
      curl -sS -f -X "$method" "$url" \
        "${AUTH_HEADER[@]}" \
        -H "Content-Type: application/json" \
        -d "$payload" > /dev/null 2>&1 && echo 1 >> "$ok_file" &
    else
      curl -sS -f -X "$method" "$url" \
        "${AUTH_HEADER[@]}" > /dev/null 2>&1 && echo 1 >> "$ok_file" &
    fi
  done

  wait

  end_ts=$(date +%s.%N)
  duration=$(echo "$end_ts - $start_ts" | bc)
  success=$(wc -l < "$ok_file" | tr -d ' ')
  rps=$(calc_rate "$success" "$duration")

  echo "[$name]"
  echo "  Requests: $total_requests"
  echo "  Exitosas: $success"
  echo "  Duracion: ${duration}s"
  echo "  Throughput: ${rps} req/s"
}

report_resource_metrics() {
  echo "\nUso de recursos bajo carga (si Actuator metrics esta expuesto):"

  local cpu mem
  cpu=$(curl -sS -f -X GET "$BASE_URL/actuator/metrics/system.cpu.usage" "${AUTH_HEADER[@]}" 2>/dev/null | jq -r '.measurements[0].value // empty' || true)
  mem=$(curl -sS -f -X GET "$BASE_URL/actuator/metrics/jvm.memory.used" "${AUTH_HEADER[@]}" 2>/dev/null | jq -r '.measurements[0].value // empty' || true)

  if [ -n "$cpu" ]; then
    echo "  system.cpu.usage: $cpu"
  else
    echo "  system.cpu.usage: no disponible (endpoint no expuesto)"
  fi

  if [ -n "$mem" ]; then
    echo "  jvm.memory.used: $mem bytes"
  else
    echo "  jvm.memory.used: no disponible (endpoint no expuesto)"
  fi

  echo "  Limites del contenedor benchmark: cpu=0.5, memory=1G"
}

main() {
  wait_for_api
  authenticate
  seed_data

  echo "\n===================="
  echo "PERFORMANCE TESTING"
  echo "===================="

  measure_avg_latency "GET effective price" "$BASE_URL/products/$PRODUCT_ID/prices?date=2024-04-15" 20
  measure_avg_latency "GET price history" "$BASE_URL/products/$PRODUCT_ID/prices" 20

  run_concurrent_test \
    "Concurrent product creation" \
    1000 \
    "POST" \
    "$BASE_URL/products" \
    '{"name":"Producto benchmark","description":"Carga concurrente"}'

  run_concurrent_test \
    "Concurrent effective price queries" \
    20000 \
    "GET" \
    "$BASE_URL/products/$PRODUCT_ID/prices?date=2024-04-15"

  run_concurrent_test \
    "Concurrent price history queries" \
    15000 \
    "GET" \
    "$BASE_URL/products/$PRODUCT_ID/prices"

  report_resource_metrics

  echo "\nResumen de evaluacion:"
  echo "  - Tiempo de arranque: ${APP_STARTUP_SECONDS}s"
  echo "  - Velocidad endpoints: ver latencias promedio"
  echo "  - Exitosas por segundo: ver throughput por bloque"
  echo "  - Recursos bajo carga: metricas Actuator (si disponibles) + limites de contenedor"

  echo "\nBenchmark completed successfully!"
}

main "$@"
