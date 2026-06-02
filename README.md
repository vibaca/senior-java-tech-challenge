# Product API - Technical Challenge

API para gestionar productos y su historial de precios en el tiempo.

## Tabla de contenido

- [Requisitos](#requisitos)
- [Stack tecnico](#stack-tecnico)
- [Justificacion de decisiones tecnicas](#justificacion-de-decisiones-tecnicas)
- [Desafíos implementados](#desafios-implementados)
- [Mejoras implementadas](#mejoras-implementadas)
- [Mejoras implementadas en el `benchmark.sh`](#mejoras-implementadas-en-el-benchmarksh)
- [Reglas de dominio implementadas](#reglas-de-dominio-implementadas)
- [Inicio rapido](#inicio-rapido)
- [Tests](#tests)
- [OpenAPI y Swagger](#openapi-y-swagger)
- [Benchmark](#benchmark)

## Requisitos

- Java 21
- Docker Desktop (incluye Docker Compose)
- Make (`make --version` para verificar)

> **Nota**: El proyecto usa Gradle Wrapper (`./gradlew`), no necesitas Gradle instalado.  
> Si solo usas Docker para levantar la app, tampoco necesitas Java local.

## Stack tecnico

- Java 21
- Spring Boot 3.3.5
- Gradle Wrapper
- Docker
- Spring Data JPA
- PostgreSQL
- Spring Security
- JWT (JJWT)

## Justificacion de decisiones tecnicas

- **Spring Boot + Java 21**: combina productividad, ecosistema maduro y buen rendimiento en APIs REST; además facilita observabilidad, testing y configuración en entornos Docker.
- **PostgreSQL**: se eligió por su solidez en reglas temporales y consultas por rango de fechas; permite aprovechar índices para mantener tiempos de respuesta estables bajo carga.
- **Spring Data JPA**: reduce boilerplate y acelera desarrollo sin perder capacidad de optimización (paginación, ordenamiento y filtrado ejecutados en base de datos).
- **Screaming Architecture + Vertical Slicing**: el proyecto “grita negocio” desde su estructura (`product`, `pricing`) en lugar de “gritar framework”; esto mejora mantenibilidad, onboarding y evolución funcional por feature.
- **DDD + Hexagonal (puertos/adaptadores)**: el dominio queda aislado de infraestructura (`PricingRepository` como puerto, JPA como adaptador), facilitando pruebas, cambios tecnológicos y claridad en reglas de negocio.
- **SOLID**: se aplica mediante SRP en handlers/capas, DIP con puertos (`ProductRepository`, `PricingRepository`), e ISP/LSP/OCP en la separación de contratos de dominio y adaptadores de infraestructura.
- **JWT stateless**: autenticación sin sesión en servidor, adecuada para escalabilidad horizontal y coherente con pruebas de carga concurrentes.
- **Makefile + Docker Compose**: estandariza ejecución para evaluadores (build, test, seed, benchmark) y minimiza fricción al levantar el proyecto en diferentes máquinas.
- **Tests**: los tests usan H2 en memoria para ser rapidos y deterministas, sin necesidad de PostgreSQL.
- **Benchmark automatizado en contenedor dedicado**: permite medir arranque, latencia, throughput y comportamiento bajo carga respetando límites de CPU/memoria definidos por la prueba.

## Desafíos implementados
- Soporte de autenticación JWT en la API
- Endpoints de precios con actualización y eliminación.
- Historial con paginación, ordenamiento y filtrado.
- Script de seed para poblar datos de prueba automáticamente.
- Benchmark concurrente ejecutable desde `docker-compose.yml`.

## Mejoras implementadas
- Endpoint `GET /products` para listar todos los productos.
- El proyecto incluye un `Makefile` con todos los comandos necesarios. Ejecuta `make` para ver la ayuda:

## Mejoras implementadas en el `benchmark.sh`
- autenticación JWT al inicio
- cálculo de tiempo de arranque
- latencia promedio por endpoint
- throughput por bloque concurrente
- lectura opcional de métricas Actuator
- respeto de los límites del contenedor auxiliar

## Reglas de dominio implementadas

- `DateRange`: valida que `initDate < endDate`, soporta `endDate = null` (vigencia abierta)
- `DateRange.overlaps()`: detecta solapamientos entre rangos, incluyendo rangos abiertos
- `DateRange.contains()`: determina si una fecha cae dentro del rango
- `PriceValue`: valor siempre mayor que cero
- `Product`: nombre y descripcion no pueden estar en blanco

## Inicio rapido

```zsh
# Clonar y entrar al proyecto
cd senior-java-tech-challenge
chmod +x ./gradlew

# Levantar API + base de datos con Docker
make up

# Ejecutar las pruebas de rendimiento
make benchmark

# Ejecutar tests (unit + behavior)
make test

# Ejecutar solo unit tests
make test-unit

# Ejecutar solo behavior tests
make test-behavior

# Poblar con datos de prueba
make seed

# Abrir Swagger UI en el navegador
make swagger

# Detener todos los contenedores
make down
```

Para autenticarse en Swagger, el endpoint `POST /auth/login` usa `admin/password` como credencial de desarrollo para JWT.

## Tests

Los tests usan H2 en memoria para ser rapidos y deterministas, sin necesidad de PostgreSQL.

```zsh
make test           # Todos los tests (unit + integracion + Cucumber)
make test-unit      # Solo domain y application layer
make test-behavior  # Solo escenarios Cucumber
```
## OpenAPI y Swagger

Con la API levantada:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Autenticate desde Swagger con `POST /auth/login`, credenciales `admin/password`, copiar el token resultante y luego usa el botón **Authorize** con el esquema `bearerAuth`.

## Benchmark

### Como ejecutar la prueba de rendimiento

Ejecutar benchmark completo (API + DB + script de carga):

```zsh
make benchmark
```

O con Docker Compose directo:

```zsh
docker compose down
docker compose up --build
```