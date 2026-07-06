# TaskFlow API

TaskFlow API is a Spring Boot backend for task management. The project currently
contains only the initial application setup and a health check endpoint.

## Tech stack

- Java 21
- Spring Boot 3.3
- Maven
- Spring Web
- Spring Data JPA
- PostgreSQL
- Liquibase
- Docker Compose

## Run PostgreSQL

Start the PostgreSQL container:

```bash
docker compose up -d
```

PostgreSQL is exposed on `localhost:5433`.

## Start the application

Run the application with Maven:

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

## Health check

```http
GET http://localhost:8080/api/health
```

Expected response:

```json
{
  "status": "UP",
  "service": "TaskFlow API"
}
```
