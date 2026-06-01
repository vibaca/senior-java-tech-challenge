# Product API - Technical Challenge

API para gestionar productos y su historial de precios en el tiempo.

## Tabla de contenido

- [Requisitos](#requisitos)
- [Stack tecnico](#stack-tecnico)
- [Arquitectura](#arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Ejecucion local](#ejecucion-local)
- [Características Opcionales](#características-opcionales)
- [OpenAPI y Swagger](#openapi-y-swagger)
- [Ejecucion con Docker](#ejecucion-con-docker)
- [Benchmark](#benchmark)
- [Troubleshooting](#troubleshooting)

## Requisitos

- Java 21
- Docker Desktop (incluye Docker Compose)
- PostgreSQL 16+ (si no usas el contenedor incluido)

## Stack tecnico

- Java 21
- Spring Boot 3.3.5
- Gradle Wrapper
- Docker
- Spring Data JPA
- PostgreSQL
- Spring Security
- JWT (JJWT)

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

### Tests unitarios

Los tests cubren dominio y application layer sin Spring context, sin DB:

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
- `PricingControllerTest`: agregar precio, obtener historial, obtener precio vigente, 404 no encontrado

**Comportamiento (Cucumber + MockMvc + JPA):**
- `product-management.feature`: creacion, consulta y validaciones de producto
- `pricing-management.feature`: alta de precios, precio vigente, historial y errores de negocio
- `RunCucumberTest`: ejecuta todos los escenarios end-to-end sobre la API

Los tests de integracion usan H2 en memoria (`src/test/resources/application.yml`) para ser rapidos y deterministas. La aplicacion normal usa PostgreSQL por defecto.

Para correr todo el suite, incluyendo Cucumber:

```zsh
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew test
```

## Ejecucion local

1) Entrar al proyecto:

```zsh
cd <ruta-al-proyecto>
```

2) Dar permisos al wrapper (solo la primera vez):

```zsh
chmod +x ./gradlew
```

3) Verificar version de Java activa (debe ser 21):

```zsh
java -version
./gradlew --version
```

4) Levantar PostgreSQL local (si no lo tienes corriendo fuera de Docker):

```zsh
docker compose up -d postgres
```

5) Ejecutar tests:

```zsh
./gradlew clean test
```

6) Levantar API:

```zsh
./gradlew bootRun
```

7) Validar health endpoint:

```zsh
curl -i http://localhost:8080/actuator/health
```

8) Obtener token JWT:

```zsh
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

Respuesta esperada:

```json
{"token":"<jwt-token>"}
```

9) Probar endpoints principales (protegidos con Bearer token):

```zsh
# Reemplaza <jwt-token> con el token devuelto por /auth/login

# Crear producto
curl -X POST http://localhost:8080/products \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Zapatillas","description":"Modelo 2025"}'

# Listar productos
curl -i http://localhost:8080/products \
  -H "Authorization: Bearer <jwt-token>"

# Obtener producto (reemplace con el ID de respuesta)
curl -i http://localhost:8080/products/{productId} \
  -H "Authorization: Bearer <jwt-token>"

# Agregar precio
curl -X POST http://localhost:8080/products/{productId}/prices \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{"value":99.99,"initDate":"2024-01-01","endDate":"2024-06-30"}'

# Actualizar precio
curl -X PUT http://localhost:8080/products/{productId}/prices/{priceId} \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{"value":119.99,"initDate":"2024-01-01","endDate":"2024-06-30"}'

# Eliminar precio
curl -X DELETE http://localhost:8080/products/{productId}/prices/{priceId} \
  -H "Authorization: Bearer <jwt-token>"

# Obtener precio vigente
curl -i http://localhost:8080/products/{productId}/prices?date=2024-04-15 \
  -H "Authorization: Bearer <jwt-token>"

# Obtener historial completo
curl -i http://localhost:8080/products/{productId}/prices \
  -H "Authorization: Bearer <jwt-token>"
```

## Características Opcionales

### ✅ Paginación, Ordenamiento y Filtrado

El endpoint `GET /products/{productId}/prices` ahora soporta:

#### Parámetros de Query

**Paginación:**
- `page` (int, default: 0) - Número de página
- `size` (int, default: 10) - Registros por página (1-100)

**Ordenamiento:**
- `sort` (string, default: "initDate") - Campo: `initDate`, `endDate`, o `value`
- `direction` (string, default: "ASC") - Dirección: `ASC` o `DESC`

**Filtrado por Precio:**
- `minValue` (BigDecimal) - Precio mínimo
- `maxValue` (BigDecimal) - Precio máximo

**Filtrado por Fechas:**
- `startDate` (YYYY-MM-DD) - Precios vigentes desde esta fecha
- `endDate` (YYYY-MM-DD) - Precios vigentes hasta esta fecha

#### Ejemplos

```zsh
# Filtrar por rango de precios (entre 50 y 150)
curl -i "http://localhost:8080/products/{productId}/prices?minValue=50&maxValue=150" \
  -H "Authorization: Bearer <jwt-token>"

# Paginación: página 2 con 5 registros
curl -i "http://localhost:8080/products/{productId}/prices?page=1&size=5" \
  -H "Authorization: Bearer <jwt-token>"

# Ordenar por precio descendente
curl -i "http://localhost:8080/products/{productId}/prices?sort=value&direction=DESC" \
  -H "Authorization: Bearer <jwt-token>"

# Combinar filtros: precios entre 50-150 vigentes en 2026, ordenados por fecha
curl -i "http://localhost:8080/products/{productId}/prices?minValue=50&maxValue=150&startDate=2026-01-01&endDate=2026-12-31&sort=initDate&direction=DESC&page=0&size=10" \
  -H "Authorization: Bearer <jwt-token>"
```

#### Respuesta con Paginación

```json
{
  "prices": [
    {
      "id": "uuid-price-1",
      "value": 99.99,
      "initDate": "2026-07-01",
      "endDate": null
    },
    {
      "id": "uuid-price-2",
      "value": 89.99,
      "initDate": "2026-01-01",
      "endDate": "2026-06-30"
    }
  ],
  "page": 0,
  "size": 10,
  "total": 2,
  "totalPages": 1
}
```

Para una guía completa, ver [`PAGINATION_FILTERING_GUIDE.md`](./PAGINATION_FILTERING_GUIDE.md)

### ✅ Actualización y Eliminación de Precios

Endpoints para modificar precios existentes:

```zsh
# Actualizar un precio
curl -X PUT http://localhost:8080/products/{productId}/prices/{priceId} \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{"value":119.99,"initDate":"2024-01-01","endDate":"2024-06-30"}'

# Eliminar un precio
curl -X DELETE http://localhost:8080/products/{productId}/prices/{priceId} \
  -H "Authorization: Bearer <jwt-token>"
```

### ✅ Autenticación con JWT

La API está protegida con tokens JWT. Todos los endpoints requieren autenticación Bearer token.

```zsh
curl -i http://localhost:8080/products/{productId}/prices \
  -H "Authorization: Bearer <jwt-token>"
```

### ✅ Documentación OpenAPI/Swagger

La API está completamente documentada con Swagger/OpenAPI (ver sección [OpenAPI y Swagger](#openapi-y-swagger)).

10) Poblar datos de prueba automaticamente (manual):

```zsh
chmod +x ./scripts/seed-test-data.sh
./scripts/seed-test-data.sh
```

Opcionalmente puedes usar variables de entorno si cambian URL o credenciales:

```zsh
BASE_URL=http://localhost:8080 AUTH_USER=admin AUTH_PASSWORD=password ./scripts/seed-test-data.sh
```

## OpenAPI y Swagger

Con la API levantada, puedes consultar la documentacion interactiva aqui:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

La documentacion incluye operaciones para los features de productos y precios, con respuestas de exito y errores de negocio.

Puedes autenticarte directamente desde Swagger con el endpoint `POST /auth/login` y luego usar el boton **Authorize** con el esquema `bearerAuth`.

## Arquitectura de Controllers (SRP)

Cada controller tiene una única responsabilidad por feature:

- **ProductController**: CRUD básico de productos
  - `POST /products` → crea producto
  - `GET /products` → lista productos
  - `GET /products/{id}` → obtiene datos del producto

- **PricingController**: manejo de precios históricos
  - `POST /products/{id}/prices` → agrega precio
  - `PUT /products/{id}/prices/{priceId}` → actualiza precio
  - `DELETE /products/{id}/prices/{priceId}` → elimina precio
  - `GET /products/{id}/prices` → historial o precio vigente (con ?date)

## Persistencia

La persistencia por defecto se implementa con **Spring Data JPA + PostgreSQL**.

- `JpaProductRepository` adapta el puerto `ProductRepository`
- `JpaPricingRepository` adapta el puerto `PricingRepository`
- `ProductEntity` y `PriceEntity` representan el modelo persistente
- `SpringData...JpaRepository` encapsula las operaciones JPA/JPQL

La infraestructura de persistencia usa unicamente implementaciones JPA (sin repositorios `in-memory`).

## Ejecucion con Docker

### Solo API (recomendado para desarrollo)

```zsh
cd <ruta-al-proyecto>
docker compose down
docker compose up --build postgres app
```

### API + benchmark

```zsh
cd <ruta-al-proyecto>
docker compose down
docker compose up --build
```

### Levantar en segundo plano

```zsh
cd <ruta-al-proyecto>
docker compose up -d --build postgres app
```

### Ver estado y logs

```zsh
cd <ruta-al-proyecto>
docker compose ps
docker compose logs -f app
```

### Detener servicios

```zsh
cd <ruta-al-proyecto>
docker compose down
```

### Rebuild limpio (si hay cache)

```zsh
cd <ruta-al-proyecto>
docker compose down
docker compose build --no-cache app
docker compose up postgres app
```

## Benchmark

El servicio `benchmark` ejecuta `benchmark.sh` y realiza:

1. Espera a que `GET /actuator/health` responda OK.
2. Usa PostgreSQL como almacenamiento persistente de productos y precios.
3. Crea un producto de prueba.
4. Agrega varios precios por rango de fechas.
5. Consulta precio vigente e historial.
6. Lanza pruebas concurrentes de escritura y lectura.

## Troubleshooting

### Error: `Unsupported class file major version 69`

Este error aparece cuando hay incompatibilidad de version de Java en tiempo de build.

Pasos recomendados:

1) Confirmar que la JVM activa sea Java 21:

```zsh
java -version
./gradlew --version
```

2) Si no estas en Java 21, exportar `JAVA_HOME` a una instalacion de JDK 21 y reintentar:

```zsh
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

3) Limpiar caches locales de Gradle y recompilar:

```zsh
cd <ruta-al-proyecto>
./gradlew --stop
rm -rf ~/.gradle/caches
./gradlew clean test
```

### Error: `zsh: permission denied: ./gradlew`

```zsh
cd <ruta-al-proyecto>
chmod +x ./gradlew
```

### Error: `no main manifest attribute, in app.jar`

El artefacto no se genero como `bootJar` ejecutable de Spring Boot. Reconstruir la imagen:

```zsh
cd <ruta-al-proyecto>
docker compose down
docker compose build --no-cache app
docker compose up postgres app
```

### Error de conexion a PostgreSQL

Si ejecutas la app fuera de Docker, asegúrate de tener PostgreSQL disponible en `localhost:5432` o exporta estas variables:

```zsh
export DB_URL=jdbc:postgresql://localhost:5432/products
export DB_USERNAME=products
export DB_PASSWORD=products
```

### `benchmark` se queda en "Esperando API..."

La API no arranco correctamente. Revisar logs:

```zsh
cd <ruta-al-proyecto>
docker compose logs app --tail=100
```
