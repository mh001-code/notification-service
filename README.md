# notification-service

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white">
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white">
  <img alt="RabbitMQ" src="https://img.shields.io/badge/RabbitMQ-3-FF6600?logo=rabbitmq&logoColor=white">
  <img alt="Spring Retry" src="https://img.shields.io/badge/Spring_Retry-enabled-6DB33F?logo=spring&logoColor=white">
  <img alt="Docker" src="https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white">
  <img alt="CI" src="https://github.com/mh001-code/notification-service/actions/workflows/ci.yml/badge.svg">
</p>

Notification service of the [Order Processing System](https://github.com/mh001-code) — a microservices portfolio project demonstrating event-driven architecture, retry patterns, and resilient notification delivery.

---

## Table of Contents

- [About](#about)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Business Rules](#business-rules)
- [Running Locally](#running-locally)
- [Endpoints](#endpoints)
- [Technical Decisions](#technical-decisions)

---

## About

The `notification-service` consumes order events from RabbitMQ and records notifications for customers. It simulates email delivery with a configurable failure rate, applies automatic retry via Spring Retry, and persists the full notification history with delivery status (`PENDING` → `SENT` / `FAILED`).

```
order-service → order.created   → RabbitMQ → notification-service (ORDER_CONFIRMATION)
             → order.cancelled  →           → notification-service (ORDER_CANCELLATION)
```

---

## Architecture

The project follows **Hexagonal Architecture (Ports & Adapters)**, keeping the domain isolated from infrastructure concerns.

```
┌─────────────────────────────────────────────────────┐
│                     API Layer                        │
│           Controllers · DTOs · ExceptionHandler      │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                 Application Layer                    │
│            Use Cases · Port Interfaces               │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                  Domain Layer                        │
│  Notification · NotificationType · NotificationStatus│
└─────────────────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│              Infrastructure Layer                    │
│   JPA Adapter · RabbitMQ Consumer · MockEmailSender  │
└─────────────────────────────────────────────────────┘
```

```mermaid
graph TD
    RabbitMQ -->|order.created| OrderEventConsumer
    RabbitMQ -->|order.cancelled| OrderEventConsumer
    OrderEventConsumer --> ProcessOrderCreatedService
    OrderEventConsumer --> ProcessOrderCancelledService
    ProcessOrderCreatedService -->|port/out| NotificationJpaAdapter
    ProcessOrderCreatedService -->|port/out| MockEmailSender
    ProcessOrderCancelledService -->|port/out| NotificationJpaAdapter
    ProcessOrderCancelledService -->|port/out| MockEmailSender
    MockEmailSender -->|@Retryable 3x| EmailDelivery
    NotificationJpaAdapter -->|JPA| PostgreSQL
```

**Base package:** `com.orderprocessing.notification.service`

```
com.orderprocessing.notification.service
├── domain
│   ├── model          # Notification, NotificationType, NotificationStatus, NotificationChannel
│   └── exception      # NotificationNotFoundException
├── application
│   ├── usecase        # ProcessOrderCreatedService, ProcessOrderCancelledService
│   └── port
│       ├── in         # ProcessOrderCreatedUseCase, ProcessOrderCancelledUseCase
│       └── out        # NotificationRepositoryPort, NotificationSenderPort
├── infrastructure
│   ├── persistence    # JPA repository + adapter
│   ├── messaging      # OrderEventConsumer, MockEmailSender, event records
│   └── config         # RabbitMQConfig
└── api
    ├── controller     # NotificationController, HealthController
    ├── dto            # NotificationResponse record
    └── handler        # GlobalExceptionHandler
```

---

## Tech Stack

| Technology | Version | Role |
|---|---|---|
| [Java](https://openjdk.org/) | 17 | Primary language |
| [Spring Boot](https://spring.io/projects/spring-boot) | 3.5 | Web framework + DI |
| [Spring AMQP](https://spring.io/projects/spring-amqp) | — | RabbitMQ consumer |
| [Spring Retry](https://github.com/spring-projects/spring-retry) | — | Automatic retry on send failure |
| [Spring Data JPA](https://spring.io/projects/spring-data-jpa) | — | ORM persistence |
| [PostgreSQL](https://www.postgresql.org/) | 16 | Notification history |
| [Flyway](https://flywaydb.org/) | — | Database migrations |
| [Lombok](https://projectlombok.org/) | — | Boilerplate reduction |
| [JUnit 5 + Mockito](https://junit.org/junit5/) | — | Unit testing |
| [Testcontainers](https://testcontainers.com/) | — | Integration tests with real PostgreSQL + RabbitMQ |
| [Docker](https://www.docker.com/) | — | Containerization (multi-stage build) |
| [GitHub Actions](https://github.com/features/actions) | — | CI/CD pipeline |

---

## Business Rules

- `order.created` triggers an `ORDER_CONFIRMATION` notification to the customer
- `order.cancelled` triggers an `ORDER_CANCELLATION` notification
- Each notification starts as `PENDING`, then becomes `SENT` or `FAILED`
- The sender simulates a **10% failure rate** per attempt to reflect real-world delivery unreliability
- Spring Retry retries up to **3 times** with a **1-second backoff** before giving up
- If all retries fail, the notification is saved as `FAILED` — the RabbitMQ message is still acknowledged so the queue is never blocked
- Full notification history is persisted and queryable by `customerId` or `orderId`

### Notification lifecycle

```
Event received
      │
      ▼
Notification saved (PENDING)
      │
      ▼
notificationSender.send()
      ├── success ──────────────► markSent() → save (SENT)
      └── failure (retry x3) ──► markFailed() → save (FAILED) → ack message
```

---

## Running Locally

### Prerequisites

- Java 17+
- Docker and Docker Compose
- Maven (or use the included `./mvnw` wrapper)

### 1. Clone the repository

```bash
git clone https://github.com/mh001-code/notification-service.git
cd notification-service
```

### 2. Start PostgreSQL and RabbitMQ

```bash
docker-compose up -d
```

PostgreSQL on port `5437` · RabbitMQ on port `5672` · Management UI on `15672`

### 3. Run the application

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8082`.
RabbitMQ Management UI: http://localhost:15672 (guest / guest)

### 4. Run the tests

```bash
# All tests including integration (Docker required for Testcontainers)
./mvnw test
```

---

## Endpoints

| Method | Route | Description | Status |
|---|---|---|---|
| `GET` | `/notifications?customerId={id}` | List notifications for a customer | 200 |
| `GET` | `/notifications?orderId={id}` | List notifications for an order | 200 |
| `GET` | `/notifications/{id}` | Get a specific notification | 200 / 404 |
| `GET` | `/health` | Health check | 200 |

### HTTP Status Codes

| Status | Situation |
|---|---|
| `200 OK` | Query successful |
| `404 Not Found` | Notification not found |

---

## Technical Decisions

**`@Retryable` over manual retry loops**
Spring Retry intercepts the `send()` call via AOP and retries transparently. The use case has no retry logic — it only catches the final exception after all attempts are exhausted. This keeps the business logic clean and makes the retry policy easy to tune via annotations.

**`FAILED` status instead of re-throwing to the consumer**
A delivery failure to a specific customer should not cause infinite redelivery of the RabbitMQ message. The notification is saved as `FAILED` for audit and potential replay — the message is acknowledged and the consumer stays healthy. Separating delivery concerns from queue concerns is a key principle of resilient event-driven systems.

**`@MockBean` in integration tests**
The `MockEmailSender` has a random 10% failure rate which would make tests non-deterministic. Integration tests replace it with a `@MockBean` that succeeds or fails on demand, giving full control over SENT vs FAILED assertions without retry delays.

**Hexagonal Architecture**
`ProcessOrderCreatedService` depends on `NotificationSenderPort` — a pure interface. Swapping the mock email sender for a real SMTP adapter or an AWS SES client requires only a new infrastructure class, with zero changes to the domain or use case layer.

**Testcontainers singleton pattern**
PostgreSQL and RabbitMQ containers start once for the entire test suite and are shared across all test classes via a static initializer block. This avoids the Spring context caching issue that would occur if containers were recreated per class.

---

<p align="center">
  Built by <a href="mailto:marcioincode@gmail.com">Márcio Henrique</a>
</p>
