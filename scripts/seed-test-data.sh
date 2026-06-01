#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
AUTH_USER="${AUTH_USER:-admin}"
AUTH_PASSWORD="${AUTH_PASSWORD:-password}"

extract_json_field() {
  local json="$1"
  local field="$2"
  echo "$json" | sed -n "s/.*\"$field\":\"\([^\"]*\)\".*/\1/p"
}

login() {
  local response
  response=$(curl -sS -f -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$AUTH_USER\",\"password\":\"$AUTH_PASSWORD\"}")

  local token
  token=$(extract_json_field "$response" "token")

  if [[ -z "$token" ]]; then
    echo "[ERROR] Could not obtain JWT token. Response: $response"
    exit 1
  fi

  echo "$token"
}

create_product() {
  local token="$1"
  local name="$2"
  local description="$3"

  local response
  response=$(curl -sS -f -X POST "$BASE_URL/products" \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"$name\",\"description\":\"$description\"}")

  local product_id
  product_id=$(extract_json_field "$response" "productId")

  if [[ -z "$product_id" ]]; then
    echo "[ERROR] Could not parse productId. Response: $response"
    exit 1
  fi

  echo "$product_id"
}

add_price() {
  local token="$1"
  local product_id="$2"
  local value="$3"
  local init_date="$4"
  local end_date="$5"

  local payload
  if [[ "$end_date" == "null" ]]; then
    payload="{\"value\":$value,\"initDate\":\"$init_date\",\"endDate\":null}"
  else
    payload="{\"value\":$value,\"initDate\":\"$init_date\",\"endDate\":\"$end_date\"}"
  fi

  curl -sS -f -X POST "$BASE_URL/products/$product_id/prices" \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d "$payload" >/dev/null
}

main() {
  echo "[INFO] Seeding demo data on $BASE_URL"
  local token
  token=$(login)

  local shoes_id
  shoes_id=$(create_product "$token" "Zapatillas running" "Modelo 2026 transpirable")
  add_price "$token" "$shoes_id" "89.99" "2026-01-01" "2026-06-30"
  add_price "$token" "$shoes_id" "99.99" "2026-07-01" "null"

  local jacket_id
  jacket_id=$(create_product "$token" "Chaqueta impermeable" "Ligera para trail")
  add_price "$token" "$jacket_id" "129.99" "2026-01-01" "2026-03-31"
  add_price "$token" "$jacket_id" "149.99" "2026-04-01" "null"

  local watch_id
  watch_id=$(create_product "$token" "Reloj deportivo" "GPS y frecuencia cardiaca")
  add_price "$token" "$watch_id" "199.99" "2026-01-01" "2026-09-30"
  add_price "$token" "$watch_id" "219.99" "2026-10-01" "null"

  echo "[OK] Seed completed"
  echo "  - Zapatillas running: $shoes_id"
  echo "  - Chaqueta impermeable: $jacket_id"
  echo "  - Reloj deportivo: $watch_id"
}

main "$@"


