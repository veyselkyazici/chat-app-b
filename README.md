# Chat App Backend

This is the backend for the Chat Application, built with **Spring Boot** using a **Microservices Architecture**.

## Architecture

The system consists of the following microservices:

*   **Eureka Server**: Service Discovery (`eureka-server`)
*   **Config Server**: Centralized Configuration (`config-server`)
*   **API Gateway**: Entry point for the system (`api-gateway-service`)
*   **Auth Service**: Authentication and Authorization (`auth-service`)
*   **User Service**: User management (`user-service`)
*   **Chat Service**: Messaging logic (`chat-service`)
*   **Contacts Service**: Contact list management (`contacts-service`)
*   **Websocket Service**: Real-time communication (`websocket-service`)
*   **Mail Service**: Email notifications (`mail-service`)

## Tech Stack

*   **Java**: 25 (Spring Boot 3.5.4)
*   **Gradle**: Build automation
*   **Docker & Docker Compose**: Containerization and orchestration
*   **Databases**:
    *   **PostgreSQL**: Relational data (Auth, User, Contacts, Mail)
    *   **MongoDB**: Chat history & Messages
    *   **Redis**: Caching and Session management
*   **Message Broker**: **RabbitMQ** (Inter-service communication)

## Prerequisites

*   **Java 25** (Ensure your JDK supports Java 25 features)
*   **Docker** & **Docker Compose**
*   **Gradle** (Wrapper included)

## Getting Started

### 1. Infrastructure (Databases & Brokers)

Start the required infrastructure services (PostgreSQL, MongoDB, Redis, RabbitMQ) using Docker:

```bash
docker-compose -f docker-compose-db.yml up -d
```

### 2. Build the Microservices

Build all services using Gradle:

```bash
./gradlew build -x test
```

### 3. Running Services

You can run the full system using Docker Compose:

```bash
docker-compose -f docker-compose-services.yml up --build -d
```

**Note**: Services depend on `eureka-server` and `config-server`. The `docker-compose` file handles the startup order and health checks to ensure dependencies are ready.

### 4. Service Configuration

*   **Config Server** runs on port `8888`.
*   **Eureka Server** runs on port `8761`.
*   **API Gateway** runs on port `8080` (Main access point).

## Development

To run services individually for development:

1.  Start infrastructure (`docker-compose-db.yml`).
2.  Start **Config Server**.
3.  Start **Eureka Server**.
4.  Start other services as needed.

Ensure your `.env` file or environment variables are configured correctly for local development. `docker-compose-services.yml` uses `${DOCKER_USERNAME}` variables, so ensure your `.env` is set up.
