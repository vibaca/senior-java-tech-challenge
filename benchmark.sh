#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://product-api:8080}"
HEALTH_ENDPOINT="$BASE_URL/actuator/health"
AUTH_USER="${AUTH_USER:-admin}"
AUTH_PASSWORD="${AUTH_PASSWORD:-password}"

echo "Esperando a que la API esté lista en $HEALTH_ENDPOINT..."

sleep 5
until curl -sS "$HEALTH_ENDPOINT" | grep UP > /dev/null; do
  echo "Esperando API..."
  sleep 5
done

echo "Autenticando benchmark user..."
LOGIN_RESPONSE=$(curl -sS -f -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$AUTH_USER\",\"password\":\"$AUTH_PASSWORD\"}")

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token // empty')
if [ -z "$TOKEN" ]; then
  echo "Error: No se pudo obtener token JWT"
  echo "Response: $LOGIN_RESPONSE"
  exit 1
fi

AUTH_HEADER=("-H" "Authorization: Bearer $TOKEN")

echo "Creating product..."
PRODUCT_RESPONSE=$(curl -sS -f -X POST "$BASE_URL/products" \
  "${AUTH_HEADER[@]}" \
  -H "Content-Type: application/json" \
  -d '{"name":"Zapatillas deportivas","description":"Modelo 2025 edición limitada"}')

PRODUCT_ID=$(echo "$PRODUCT_RESPONSE" | jq -r '.productId // .id // empty')

if [ -z "$PRODUCT_ID" ]; then
  echo "Error: Could not extract product ID from response"
  echo "Response: $PRODUCT_RESPONSE"
  exit 1
fi

echo "Product created with ID: $PRODUCT_ID"
echo -e "\n"

echo "Adding first price..."
curl -sS -f -X POST "$BASE_URL/products/$PRODUCT_ID/prices" \
  "${AUTH_HEADER[@]}" \
  -H "Content-Type: application/json" \
  -d '{"value":99.99,"initDate":"2024-01-01","endDate":"2024-06-30"}'
echo -e "\n"

echo "Adding second price..."
curl -sS -f -X POST "$BASE_URL/products/$PRODUCT_ID/prices" \
  "${AUTH_HEADER[@]}" \
  -H "Content-Type: application/json" \
  -d '{"value":129.99,"initDate":"2024-07-01","endDate":"2024-12-31"}'
echo -e "\n"

echo "Adding third price..."
curl -sS -f -X POST "$BASE_URL/products/$PRODUCT_ID/prices" \
  "${AUTH_HEADER[@]}" \
  -H "Content-Type: application/json" \
  -d '{"value":199.99,"initDate":"2025-01-01","endDate":null}'
echo -e "\n"

DATE="2024-04-15"
echo "Getting price on date $DATE..."
curl -sS -f -X GET "$BASE_URL/products/$PRODUCT_ID/prices?date=$DATE" \
  "${AUTH_HEADER[@]}"
echo -e "\n"

DATE2="2024-08-15"
echo "Getting price on date $DATE2..."
curl -sS -f -X GET "$BASE_URL/products/$PRODUCT_ID/prices?date=$DATE2" \
  "${AUTH_HEADER[@]}"
echo -e "\n"

DATE3="2025-03-01"
echo "Getting current price on date $DATE3..."
curl -sS -f -X GET "$BASE_URL/products/$PRODUCT_ID/prices?date=$DATE3" \
  "${AUTH_HEADER[@]}"
echo -e "\n"

echo "Getting full price history..."
curl -sS -f -X GET "$BASE_URL/products/$PRODUCT_ID/prices" \
  "${AUTH_HEADER[@]}"
echo -e "\n"

echo "===================="
echo "PERFORMANCE TESTING"
echo "===================="

echo "Testing concurrent product creation..."
START_TIME=$(date +%s.%N)
for i in {1..1000}; do
  curl -sS -f -X POST "$BASE_URL/products" \
    "${AUTH_HEADER[@]}" \
    -H "Content-Type: application/json" \
    -d '{"name":"Producto Test '"$i"'","description":"Descripción del producto '"$i"'"}' &
done
wait
END_TIME=$(date +%s.%N)
DURATION=$(echo "$END_TIME - $START_TIME" | bc)
echo "1000 concurrent product creations took: $DURATION seconds"
echo -e "\n"

# Test concurrent price queries
echo "Testing concurrent price queries..."
START_TIME=$(date +%s.%N)
for i in {1..20000}; do
  curl -sS -f -X GET "$BASE_URL/products/$PRODUCT_ID/prices?date=2024-04-15" \
    "${AUTH_HEADER[@]}" > /dev/null &
done
wait
END_TIME=$(date +%s.%N)
DURATION=$(echo "$END_TIME - $START_TIME" | bc)
echo "20000 concurrent price queries took: $DURATION seconds"
echo -e "\n"

# Test concurrent price history requests
echo "Testing concurrent price history requests..."
START_TIME=$(date +%s.%N)
for i in {1..15000}; do
  curl -sS -f -X GET "$BASE_URL/products/$PRODUCT_ID/prices" \
    "${AUTH_HEADER[@]}" > /dev/null &
done
wait
END_TIME=$(date +%s.%N)
DURATION=$(echo "$END_TIME - $START_TIME" | bc)
echo "15000 concurrent price history requests took: $DURATION seconds"
echo -e "\n"

echo "Benchmark completed successfully!"