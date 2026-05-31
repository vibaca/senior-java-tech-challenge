# Product API - Technical Challenge

API para gestionar productos y su historial de precios en el tiempo.

## Tabla de contenido

- [Requisitos](#requisitos)
- [Stack tecnico](#stack-tecnico)
- [Arquitectura](#arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Ejecucion local](#ejecucion-local)
- [Ejecucion con Docker](#ejecucion-con-docker)
- [Benchmark](#benchmark)
- [Troubleshooting](#troubleshooting)

## Requisitos

- Java 21
- Docker Desktop (incluye Docker Compose)

## Stack tecnico

- Java 21
- Spring Boot 3.3.5
- Gradle Wrapper
- Docker
- PostgreSQL (pendiente de configurar)

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
    application/               <- (pendiente: handlers CQRS)
    infrastructure/            <- (pendiente: adaptadores JPA)
    api/                       <- (pendiente: REST controllers)

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
    application/               <- (pendiente: handlers CQRS)
    infrastructure/            <- (pendiente: adaptadores JPA)
    api/                       <- (pendiente: REST controllers)
```

### Reglas de dominio implementadas

- `DateRange`: valida que `initDate < endDate`, soporta `endDate = null` (vigencia abierta)
- `DateRange.overlaps()`: detecta solapamientos entre rangos, incluyendo rangos abiertos
- `DateRange.contains()`: determina si una fecha cae dentro del rango
- `PriceValue`: valor siempre mayor que cero
- `Product`: nombre y descripcion no pueden estar en blanco

### Tests unitarios

Los tests cubren el dominio puro, sin Spring context, sin DB:

- `ProductTest`: creacion valida e invalida de producto
- `DateRangeTest`: rangos cerrados, abiertos, solapamientos, contiene fecha
- `PriceTest`: efectividad en fecha, solapamiento entre precios, validaciones de valor

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

4) Ejecutar tests:

```zsh
./gradlew clean test
```

5) Levantar API:

```zsh
./gradlew bootRun
```

6) Validar health endpoint:

```zsh
curl -i http://localhost:8080/actuator/health
```

## Ejecucion con Docker

### Solo API (recomendado para desarrollo)

```zsh
cd <ruta-al-proyecto>
docker compose down
docker compose up --build app
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
docker compose up -d --build app
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
docker compose up app
```

## Benchmark

El servicio `benchmark` ejecuta `benchmark.sh` y realiza:

1. Espera a que `GET /actuator/health` responda OK.
2. Crea un producto de prueba.
3. Agrega varios precios por rango de fechas.
4. Consulta precio vigente e historial.
5. Lanza pruebas concurrentes de escritura y lectura.

## Troubleshooting

### Error: `Unsupported class file major version 69`

Este error aparece cuando hay incompatibilidad de version de Java en tiempo de build.

Pasos recomendados:

1) Confirmar que la JVM activa sea Java 21:

```zsh
java -version
./gradlew --version
```

2) Si no estas en Java 21, exportar `JAVA_HOME` a una instalacion de JDK 21 y reintentar.

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
cd /Users/isai/projects/senior-java-tech-challenge
docker compose down
docker compose build --no-cache app
docker compose up app
```

### `benchmark` se queda en "Esperando API..."

La API no arranco correctamente. Revisar logs:

```zsh
cd /Users/isai/projects/senior-java-tech-challenge
docker compose logs app --tail=100
```
