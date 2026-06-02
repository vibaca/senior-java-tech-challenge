# Product API - Technical Challenge

API para gestionar productos y su historial de precios en el tiempo.

## Tabla de contenido

- [Requisitos](#requisitos)
- [Stack tecnico](#stack-tecnico)
- [Inicio rapido](#inicio-rapido)
- [Makefile](#makefile)
- [Arquitectura](#arquitectura)
- [Tests](#tests)
- [Características Opcionales](#características-opcionales)
- [OpenAPI y Swagger](#openapi-y-swagger)
- [Arquitectura de Controllers](#arquitectura-de-controllers-srp)
- [Persistencia](#persistencia)
- [Benchmark](#benchmark)
- [Troubleshooting](#troubleshooting)

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

## Inicio rapido

```zsh
# Clonar y entrar al proyecto
cd senior-java-tech-challenge
chmod +x ./gradlew

# Levantar API + base de datos con Docker
make up

# Poblar con datos de prueba
make seed

# Abrir Swagger UI en el navegador
make swagger
```

## Makefile

El proyecto incluye un `Makefile` con todos los comandos necesarios. Ejecuta `make` para ver la ayuda:

```
make
```

### Comandos disponibles

| Comando | Descripción |
|---------|-------------|
| `make build` | Compila el proyecto (sin tests) |
| `make test` | Ejecuta todos los tests |
| `make test-unit` | Solo unit tests (domain + application) |
| `make test-behavior` | Solo tests de comportamiento (Cucumber) |
| `make clean` | Limpia artefactos de build |
| `make run` | Levanta la app localmente (levanta PostgreSQL automáticamente) |
| `make db-up` | Levanta solo PostgreSQL en Docker |
| `make db-down` | Detiene PostgreSQL |
| `make up` | Levanta API + PostgreSQL con Docker Compose |
| `make down` | Detiene todos los contenedores |
| `make rebuild` | Rebuild limpio de la imagen (sin cache) |
| `make logs` | Logs de la API en tiempo real |
| `make seed` | Puebla la DB con datos de prueba |
| `make swagger` | Abre Swagger UI en el navegador |
| `make benchmark` | Ejecuta el benchmark completo |

### Variables sobrescribibles

```zsh
make seed BASE_URL=http://localhost:9090
make seed AUTH_USER=admin AUTH_PASS=otraclave
```

## Arquitectura

El proyecto sigue **Vertical Slicing** con **Screaming Architecture**, aplicando principios de **DDD**, **Arquitectura Hexagonal**, **SOLID** y preparado para **CQRS**.

### Por que Vertical Slicing

Cada feature (`product`, `pricing`) es autocontenida. El evaluador puede entender el sistema leyendo un solo feature de arriba a abajo, sin saltar entre capas horizontales.

### Estructura de paquetes

```
com.mango.products/
  product/                     <- feature: gestion de productos
    domain/
      model/
        Product.java           <- Aggregate Root
      valueobject/
        ProductId.java         <- Value Object
      repository/
        ProductRepository.java <- Puerto saliente (interfaz)
    application/
      command/
        CreateProductCommand.java
        CreateProductHandler.java
      query/
        GetProductQuery.java
        GetProductHandler.java
    infrastructure/
      persistence/
        JpaProductRepository.java
      persistence/entity/
        ProductEntity.java
      persistence/jpa/
        SpringDataProductJpaRepository.java
    api/
      controller/
        ProductController.java
      dto/
        CreateProductRequest.java
        CreateProductResponse.java

  pricing/                     <- feature: precios historicos
    domain/
      model/
        Price.java
      valueobject/
        PriceId.java
        PriceValue.java
        DateRange.java
      repository/
        PricingRepository.java <- Puerto saliente (interfaz)
      exception/
        DomainException.java
        InvalidDateRangeException.java
        PriceOverlapException.java
        PriceNotFoundException.java
    application/
      command/
        AddPriceCommand.java
        AddPriceHandler.java
      query/
        GetEffectivePriceQuery.java
        GetEffectivePriceHandler.java
        GetPriceHistoryQuery.java
        GetPriceHistoryHandler.java
    infrastructure/
      persistence/
        JpaPricingRepository.java
      persistence/entity/
        PriceEntity.java
      persistence/jpa/
        SpringDataPriceJpaRepository.java
    api/
      controller/
        PricingController.java
      dto/
        AddPriceRequest.java
        PriceDTO.java
        PriceFilterCriteria.java
        PriceHistoryPageResponse.java

  config/
    ApplicationConfig.java           <- Beans de handlers
    GlobalExceptionHandler.java     <- Manejo global de excepciones
```

### Reglas de dominio implementadas

- `DateRange`: valida que `initDate < endDate`, soporta `endDate = null` (vigencia abierta)
- `DateRange.overlaps()`: detecta solapamientos entre rangos, incluyendo rangos abiertos
- `DateRange.contains()`: determina si una fecha cae dentro del rango
- `PriceValue`: valor siempre mayor que cero
- `Product`: nombre y descripcion no pueden estar en blanco

## Tests

Los tests usan H2 en memoria para ser rapidos y deterministas, sin necesidad de PostgreSQL.

```zsh
make test           # Todos los tests (unit + integracion + Cucumber)
make test-unit      # Solo domain y application layer (sin Spring, sin DB)
make test-behavior  # Solo escenarios Cucumber
```

### Cobertura

**Dominio:**
- `ProductTest`: creacion valida e invalida de producto
- `DateRangeTest`: rangos cerrados, abiertos, solapamientos, contiene fecha
- `PriceTest`: efectividad en fecha, solapamiento entre precios, validaciones de valor

**Application (con Mockito):**
- `CreateProductHandlerTest`: creacion, verificacion de save, validaciones
- `GetProductHandlerTest`: producto encontrado, producto no encontrado
- `AddPriceHandlerTest`: precio guardado, solapamiento, producto no encontrado, rango abierto
- `GetEffectivePriceHandlerTest`: precio vigente encontrado, no encontrado
- `GetPriceHistoryHandlerTest`: historial completo, lista vacia, producto no encontrado

**Integracion (MockMvc + JPA):**
- `ProductControllerTest`: crear producto, obtener producto, 404 cuando no existe
- `PricingControllerTest`: agregar precio, historial, precio vigente, paginacion, filtrado, ordenamiento

**Comportamiento (Cucumber + MockMvc + JPA):**
- `product-management.feature`: creacion, consulta y validaciones de producto
- `pricing-management.feature`: alta de precios, precio vigente, historial y errores de negocio

## Características Opcionales

### ✅ Paginación, Ordenamiento y Filtrado

El endpoint `GET /products/{productId}/prices` soporta:

| Parámetro | Default | Descripción |
|-----------|---------|-------------|
| `page` | 0 | Número de página |
| `size` | 10 | Registros por página (1-100) |
| `sort` | initDate | Campo: `initDate`, `endDate`, `value` |
| `direction` | ASC | Dirección: `ASC` o `DESC` |
| `minValue` | - | Precio mínimo |
| `maxValue` | - | Precio máximo |
| `startDate` | - | Precios vigentes desde esta fecha |
| `endDate` | - | Precios vigentes hasta esta fecha |

```zsh
# Filtrar por rango de precios, ordenado y paginado
curl "http://localhost:8080/products/{id}/prices?minValue=50&maxValue=150&sort=value&direction=DESC&page=0&size=5" \
  -H "Authorization: Bearer <token>"
```

Respuesta:

```json
{
  "prices": [
    { "id": "uuid", "value": 99.99, "initDate": "2026-01-01", "endDate": "2026-06-30" }
  ],
  "page": 0,
  "size": 5,
  "total": 1,
  "totalPages": 1
}
```

### ✅ Actualización y Eliminación de Precios

```zsh
# Actualizar precio
curl -X PUT http://localhost:8080/products/{id}/prices/{priceId} \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"value":119.99,"initDate":"2024-01-01","endDate":"2024-06-30"}'

# Eliminar precio
curl -X DELETE http://localhost:8080/products/{id}/prices/{priceId} \
  -H "Authorization: Bearer <token>"
```

### ✅ Autenticación con JWT

Todos los endpoints requieren Bearer token:

```zsh
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
# → {"token":"<jwt-token>"}
```

### ✅ Documentación OpenAPI/Swagger

```zsh
make swagger   # Abre Swagger UI en el navegador
```

O manualmente: `http://localhost:8080/swagger-ui/index.html`

### ✅ Seed de datos de prueba

```zsh
make seed
```

## OpenAPI y Swagger

Con la API levantada:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Autenticate desde Swagger con `POST /auth/login` y luego usa el botón **Authorize** con el esquema `bearerAuth`.

## Arquitectura de Controllers (SRP)

- **ProductController**
  - `POST /products` → crea producto
  - `GET /products` → lista productos
  - `GET /products/{id}` → obtiene datos del producto

- **PricingController**
  - `POST /products/{id}/prices` → agrega precio
  - `PUT /products/{id}/prices/{priceId}` → actualiza precio
  - `DELETE /products/{id}/prices/{priceId}` → elimina precio
  - `GET /products/{id}/prices` → historial paginado/filtrado o precio vigente (con `?date`)

## Persistencia

La persistencia se implementa con **Spring Data JPA + PostgreSQL**.

- `JpaProductRepository` adapta el puerto `ProductRepository`
- `JpaPricingRepository` adapta el puerto `PricingRepository`
- `PriceSpecifications` construye queries dinámicas con JPA Specifications para filtrado

## Benchmark

Ejecutar benchmark completo (API + DB + script de carga):

```zsh
make benchmark
```

O con Docker Compose directo:

```zsh
docker compose down
docker compose up --build
```

El servicio `benchmark` ejecuta `benchmark.sh` y realiza:

1. Espera a que `GET /actuator/health` responda OK.
2. Hace login JWT (`/auth/login`) y usa Bearer token en requests protegidos.
3. Crea producto y precios de prueba.
4. Ejecuta carga concurrente:
   - 1000 creaciones de producto.
   - 20000 consultas de precio por fecha.
   - 15000 consultas de historial.

### Restricciones de recursos (cumplimiento)

En `docker-compose.yml` se respetan los límites de la prueba:

- **app**:
  - `limits.cpus: '1.0'`
  - `limits.memory: 1G`
- **benchmark** (contenedor auxiliar):
  - `limits.cpus: '0.5'`
  - `limits.memory: 1G`

Con esto se mantiene el contenedor auxiliar en el máximo permitido (500m CPU y 1GB RAM).

### Qué se observa durante la prueba

- Tiempo de arranque de la app (en logs de `product-api`).
- Duración total de cada bloque concurrente (salida de `benchmark.sh`).
- Funcionamiento bajo carga con restricciones de CPU/memoria.

Para ver logs en tiempo real:

```zsh
make logs
docker compose logs -f benchmark
```

## Troubleshooting

### Error: `Unsupported class file major version 69`

```zsh
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew --stop
rm -rf ~/.gradle/caches
make test
```

### Error: `zsh: permission denied: ./gradlew`

```zsh
chmod +x ./gradlew
```

### Error: `no main manifest attribute, in app.jar`

```zsh
make rebuild
```

### Error de conexion a PostgreSQL

```zsh
export DB_URL=jdbc:postgresql://localhost:5432/products
export DB_USERNAME=products
export DB_PASSWORD=products
```

### `benchmark` se queda en "Esperando API..."

```zsh
make logs
```
