# Product API - Technical Challenge

API para gestionar productos y su historial de precios en el tiempo.

## Tabla de contenido

- [Requisitos](#requisitos)
- [Stack tecnico](#stack-tecnico)
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
