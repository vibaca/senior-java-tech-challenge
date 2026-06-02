.PHONY: help build test test-unit test-behavior clean run db-up db-down \
        up down logs rebuild seed swagger benchmark

BASE_URL  ?= http://localhost:8080
AUTH_USER ?= admin
AUTH_PASS ?= password

# ── Ayuda ────────────────────────────────────────────────────────────────────
help:
	@echo ""
	@echo "  Comandos disponibles"
	@echo "  ─────────────────────────────────────────────"
	@echo "  make build          Compila el proyecto (sin tests)"
	@echo "  make test           Ejecuta todos los tests"
	@echo "  make test-unit      Solo unit tests (domain + application)"
	@echo "  make test-behavior  Solo tests de comportamiento (Cucumber)"
	@echo "  make clean          Limpia artefactos de build"
	@echo "  make run            Levanta la app localmente"
	@echo ""
	@echo "  make db-up       Levanta solo PostgreSQL"
	@echo "  make db-down     Detiene PostgreSQL"
	@echo ""
	@echo "  make up          Levanta API + DB con Docker"
	@echo "  make down        Detiene todos los contenedores"
	@echo "  make rebuild     Rebuild limpio (sin cache)"
	@echo "  make logs        Logs de la API en tiempo real"
	@echo ""
	@echo "  make seed        Puebla la DB con datos de prueba"
	@echo "  make swagger     Abre Swagger UI en el navegador"
	@echo "  make benchmark   Ejecuta el benchmark completo"
	@echo ""

# ── Gradle ───────────────────────────────────────────────────────────────────
build:
	./gradlew clean build -x test

test:
	./gradlew clean test

test-unit:
	./gradlew clean test \
	  --tests "com.mango.products.pricing.domain.*" \
	  --tests "com.mango.products.pricing.application.*" \
	  --tests "com.mango.products.product.domain.*" \
	  --tests "com.mango.products.product.application.*"

test-behavior:
	./gradlew clean test --tests "com.mango.products.behavior.RunCucumberTest"

clean:
	./gradlew clean

run: db-up
	./gradlew bootRun

# ── Base de datos ─────────────────────────────────────────────────────────────
db-up:
	docker compose up -d postgres

db-down:
	docker compose stop postgres && docker compose rm -f postgres

# ── Docker ────────────────────────────────────────────────────────────────────
up:
	docker compose up -d --build postgres app

down:
	docker compose down

rebuild:
	docker compose down
	docker compose build --no-cache app
	docker compose up -d postgres app

logs:
	docker compose logs -f app

# ── Utilidades ────────────────────────────────────────────────────────────────
seed:
	@chmod +x ./scripts/seed-test-data.sh
	@BASE_URL=$(BASE_URL) AUTH_USER=$(AUTH_USER) AUTH_PASSWORD=$(AUTH_PASS) \
	  ./scripts/seed-test-data.sh

swagger:
	@open "$(BASE_URL)/swagger-ui/index.html" 2>/dev/null || \
	  xdg-open "$(BASE_URL)/swagger-ui/index.html" 2>/dev/null || \
	  echo "Abre en tu navegador: $(BASE_URL)/swagger-ui/index.html"

benchmark:
	docker compose up --build

