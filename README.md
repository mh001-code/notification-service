# notification-service

[![CI](https://github.com/mh001-code/notification-service/actions/workflows/ci.yml/badge.svg)](https://github.com/mh001-code/notification-service/actions/workflows/ci.yml)

Notification service of the [Order Processing System](https://github.com/mh001-code) — a microservices portfolio project demonstrating event-driven architecture, retry patterns, and resilient notification delivery.

## Overview

The `notification-service` consumes order events from RabbitMQ and records notifications for customers. It simulates email delivery with a configurable failure rate, applies automatic retry, and persists the full notification history with delivery status.

```
order-service → order.created   → RabbitMQ → notification-service (ORDER_CONFIRMATION)
             → order.cancelled  →           → notification-service (ORDER_CANCELLATION)
```

## Architecture

Hexagonal (ports-and-adapters) architecture:

```
com.orderprocessing.notification.service
├── domain/         # Notification, NotificationType, NotificationStatus, NotificationChannel
├── application/    # Use cases (port/in), repository & sender interfaces (port/out)
├── infrastructure/ # JPA adapter, RabbitMQ consumer, MockEmailSender, Spring config
└── api/            # REST controllers, DTOs, global exception handler
```

## Tech Stack

| Technology | Role |
|---|---|
| Java 17 | Language |
| Spring Boot 3.5 | Framework |
| Spring AMQP | RabbitMQ consumer |
| Spring Retry | Automatic retry on send failure |
| PostgreSQL 16 | Notification history persistence |
| Flyway | DB migrations |
| JUnit 5 + Mockito | Unit tests |
| Testcontainers | Integration tests |
| Docker | Containerization |
| GitHub Actions | CI/CD |

## Endpoints

| Method | Route | Description | Status |
|---|---|---|---|
| `GET` | `/notifications?customerId={id}` | List notifications for a customer | 200 |
| `GET` | `/notifications?orderId={id}` | List notifications for an order | 200 |
| `GET` | `/notifications/{id}` | Get a specific notification | 200 / 404 |
| `GET` | `/health` | Health check | 200 |

## Event Consumption

| Event | Routing Key | Notification Type |
|---|---|---|
| `OrderCreatedEvent` | `order.created` | `ORDER_CONFIRMATION` |
| `OrderCancelledEvent` | `order.cancelled` | `ORDER_CANCELLATION` |

### Notification lifecycle

```
Event received → Notification saved (PENDING) → sender.send() → SENT
                                                              ↘ FAILED (on exhausted retries)
```

Failed messages (unexpected errors) are routed to Dead Letter Queues for analysis.

## Retry Pattern

`MockEmailSender` simulates real-world delivery with a **10% failure rate per attempt**:

- Spring Retry intercepts failures and retries up to **3 times** with a **1-second backoff**
- If all 3 attempts fail, `@Recover` logs the exhaustion and re-throws
- The use case catches the exception, marks the notification as `FAILED`, and saves — the RabbitMQ message is **acknowledged regardless**, so the queue is never blocked by a persistently failing recipient

**Why FAILED doesn't re-throw to the consumer:** a bad recipient address should not cause infinite redelivery. The notification is saved for audit; ops can inspect `FAILED` records and replay if needed.

## Running Locally

**Prerequisites:** Java 17, Maven, Docker

```bash
# Start PostgreSQL (port 5437) and RabbitMQ
docker-compose up -d

# Run the service
./mvnw spring-boot:run
```

RabbitMQ Management UI: http://localhost:15672 (guest / guest)

## Running Tests

```bash
# Unit tests only (no Docker required)
./mvnw test -Dtest="ProcessOrderCreatedServiceTest,ProcessOrderCancelledServiceTest"

# All tests including integration (Docker required)
./mvnw test
```

**Test coverage:**
- `ProcessOrderCreatedServiceTest` — 2 unit tests: SENT on success, FAILED on sender exception
- `ProcessOrderCancelledServiceTest` — 2 unit tests: SENT on success, FAILED on sender exception
- `NotificationControllerIntegrationTest` — 4 integration tests: query by customerId, orderId, by ID, 404
- `OrderEventConsumerIntegrationTest` — 3 integration tests: confirmation saved SENT, confirmation saved FAILED, cancellation saved SENT

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5437/notifications` | DB connection |
| `SPRING_DATASOURCE_USERNAME` | `notifications_user` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `notifications_pass` | DB password |
| `SPRING_RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `SPRING_RABBITMQ_PORT` | `5672` | RabbitMQ port |
| `SPRING_RABBITMQ_USERNAME` | `guest` | RabbitMQ user |
| `SPRING_RABBITMQ_PASSWORD` | `guest` | RabbitMQ password |
| `PORT` | `8082` | Server port |

## Docker

```bash
# Build the image
docker build -t notification-service .

# Run (requires PostgreSQL and RabbitMQ reachable via env vars)
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5437/notifications \
  -e SPRING_DATASOURCE_USERNAME=notifications_user \
  -e SPRING_DATASOURCE_PASSWORD=notifications_pass \
  -e SPRING_RABBITMQ_HOST=host.docker.internal \
  notification-service
```

## Deploy on Railway

1. Inside the existing Order Processing System project on [Railway](https://railway.app), add a **New Service → GitHub Repo** pointing to this repository
2. Add a new **PostgreSQL** plugin for the notifications database
3. Point to the **same RabbitMQ** instance used by `order-service` and `inventory-service`
4. Set the following environment variables:

| Variable | Source |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| `SPRING_DATASOURCE_USERNAME` | `${{Postgres.PGUSER}}` |
| `SPRING_DATASOURCE_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `SPRING_RABBITMQ_HOST` | `${{rabbitmq.RAILWAY_PRIVATE_DOMAIN}}` |
| `SPRING_RABBITMQ_PORT` | `5672` |
| `SPRING_RABBITMQ_USERNAME` | `guest` |
| `SPRING_RABBITMQ_PASSWORD` | `guest` |

## Related Services

- [order-service](https://github.com/mh001-code/order-service) — entry point, publishes `order.created` / `order.cancelled`
- [inventory-service](https://github.com/mh001-code/inventory-service) — consumes the same events, manages stock
