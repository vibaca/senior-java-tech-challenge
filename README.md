# Product API - Technical Challenge

API para gestionar productos y su historial de precios en el tiempo.

## Tabla de contenido

- [Requisitos](#requisitos)
- [Stack tecnico](#stack-tecnico)
- [Ejecucion con Docker](#ejecucion-con-docker)

## Requisitos previos
Para ejecutar este proyecto, necesitas tener instalado:

* **Docker Desktop**: Incluye Docker Engine y Docker Compose.

## Stack técnico
* **Lenguaje**: Java 21
* **Gestor de dependencias**: Gradle (Wrapper)
* **Contenedorización**: Docker

## Ejecucion con Docker

### Solo API (recomendado para desarrollo)

```zsh
cd /senior-java-tech-challenge
docker compose down
docker compose up --build app
```

### API + benchmark

```zsh
cd /senior-java-tech-challenge
docker compose down
docker compose up --build
```

### Levantar en segundo plano

```zsh
cd /senior-java-tech-challenge
docker compose up -d --build app
```

### Ver estado y logs

```zsh
cd /senior-java-tech-challenge
docker compose ps
docker compose logs -f app
```

### Detener servicios

```zsh
cd /senior-java-tech-challenge
docker compose down
```

### Rebuild limpio (si hay cache)

```zsh
cd /senior-java-tech-challenge
docker compose down
docker compose build --no-cache app
docker compose up app
```