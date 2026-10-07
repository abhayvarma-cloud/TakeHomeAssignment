# demo

Spring Boot 3.2 / Java 17 service that exposes a CRUD API for `Item` resources backed by
PostgreSQL, and publishes create/delete events to Kafka.

## Stack

- Spring Boot 3.2 (Web, Data JPA, Actuator)
- PostgreSQL 15
- Apache Kafka (via Confluent images) for event publishing
- Maven (wrapper included, no local Maven install required)

## Prerequisites

- JDK 17
- Docker and Docker Compose (for running dependencies, or the whole stack)

## Running locally

### Option A: Full stack via Docker Compose

Builds the app image and starts it alongside Postgres, Zookeeper, and Kafka:

```bash
docker compose up --build
```

The API is then available at `http://localhost:8080`.

### Option B: App on host, dependencies in Docker

Start only the infrastructure:

```bash
docker compose up db zookeeper kafka
```

Then run the app with the wrapper:

```bash
./mvnw spring-boot:run
```

## Building and testing

```bash
./mvnw -s maven-settings.xml verify
```

`maven-settings.xml` points dependency resolution at public Maven Central and is used by
both local builds and CI. `verify` compiles the code and runs the test suite
(`src/test/java`).

## Configuration

Configuration lives in `src/main/resources/application.properties`. Connection details
default to local values and can be overridden via environment variables (used by
`docker-compose.yml`):

| Property | Env var | Default |
|---|---|---|
| Datasource URL | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/mydb` |
| Datasource username | `SPRING_DATASOURCE_USERNAME` | `user` |
| Datasource password | `SPRING_DATASOURCE_PASSWORD` | `password` |
| Kafka bootstrap servers | `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |

Actuator health and info endpoints are exposed at `/actuator/health` and `/actuator/info`.

## API

Base path: `/api/items`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/items` | List all items |
| `GET` | `/api/items/{id}` | Get an item by id, `404` if missing |
| `POST` | `/api/items` | Create an item, publishes a `CREATED` Kafka event |
| `DELETE` | `/api/items/{id}` | Delete an item, `404` if missing, publishes a `DELETED` Kafka event |
| `GET` | `/api/items?size=100&page=20` | List 100 items for page 20, items are deterministically loaded by changing the page no., if size changed it will shift page numbers | `GET` | `/api/items?size=100` | List 100 latest  items |

Item events are published to the `item-events` Kafka topic and consumed by
`ItemEventConsumer` (with retry via `@RetryableTopic`).

## CI

`.github/workflows/build.yml` runs `./mvnw -s maven-settings.xml -B verify` with JDK 17 on
every push and pull request to `master`, and uploads Surefire test reports as a build
artifact.
