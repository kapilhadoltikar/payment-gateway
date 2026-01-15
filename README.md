# Payment Gateway - Microservices Architecture

A secure, scalable payment gateway built with Java 25, Spring Boot, and modern cloud-native technologies.

## Architecture

This payment gateway uses a **microservices architecture** with the following components:

- **api-gateway**: Entry point using Spring Cloud Gateway (Nginx Ingress for external routing)
- **auth-service**: OAuth2 Authorization Server for JWT token management
- **payment-service**: Core payment processing logic
- **merchant-service**: Merchant onboarding and configuration
- **vault-service**: PCI-compliant tokenization and encryption
- **notification-service**: Webhook and notification dispatcher

## 🛠️ Tech Stack

- **Core Framework**: Java 25 (Virtual Threads) & Spring Boot 4.0.1 (Spring Framework 7)
- **Architecture**: Event-Driven Microservices
- **Security**: Spring Security 7, OAuth2, JWT, AES-256 Encryption (PCI-DSS compliant patterns)
- **Database**: PostgreSQL 16 (Read-Write Split Architecture), Redis 7 (Distributed Cache & Rate Limiting)
- **Messaging**: Apache Kafka 3.7 (Event Sourcing), RabbitMQ 3.13 (Async Notifications)
- **Observability**: OpenTelemetry, Grafana Tempo (Tracing), Prometheus (Metrics), Grafana (Dashboards)
- **Infrastructure**: Docker, Docker Compose, Kubernetes (Manifests included)

## ✨ Key Features

✅ **High-Performance Concurrency**: Leveraging **Java 25 Virtual Threads** to handle massive throughput with minimal resource overhead.
✅ **Scalable Persistence**: Implemented **Read-Write Splitting** routing logic to optimize database performance (Writes → Primary, Reads → Replica).
✅ **Event-Driven Reliability**: Asynchronous transaction processing via **Kafka** ensures data consistency and system resilience.
✅ **Distributed Tracing**: End-to-end visibility using **OpenTelemetry** and **Grafana Tempo** for rapid debugging of distributed transactions.
✅ **Secure Tokenization**: Dedicated **Vault Service** handles sensitive card data using hardware-agnostic AES-256-GCM encryption.
✅ **Robust Webhooks**: **RabbitMQ** backed dispatcher processes merchant notifications with automatic retries and dead-letter queues.

## Prerequisites

- Java 25 (JDK)
- Maven 3.9+
- Docker & Docker Desktop

## Getting Started

### 1. Build and Run (Single Command)
Run this in the root directory:
```bash
docker-compose up -d --build
```
This command will:
1.  Spin up all infrastructure (PostgreSQL, Kafka, Redis, etc.).
2.  Compile the Java microservices inside a Docker container (no local Java/Maven required).
3.  Deploy the services.

*Note: The first build may take a few minutes to download dependencies.*

## 🚀 API Usage Guide

The gateway exposes all services under the prefix `/api/v1/`.

### 1. Authentication
**POST** `/api/v1/auth/register`
```json
{
  "username": "merchant_user",
  "password": "secure_password",
  "email": "merchant@example.com"
}
```
**Returns**: JWT Token in `data.token`.

### 2. Merchant Onboarding
*Requires `Authorization: Bearer <token>`*  
**POST** `/api/v1/merchants`
```json
{
  "name": "Global Store",
  "email": "store@global.com",
  "webhookUrl": "http://your-app.com/webhook"
}
```
**Returns**: `merchantId` and `apiKey`.

### 3. Payment Processing
*Requires `Authorization: Bearer <token>`*  
**POST** `/api/v1/payments/process`
```json
{
  "merchantId": "PASTE_YOUR_MERCHANT_ID",
  "amount": 150.00,
  "currency": "USD",
  "paymentMethod": "CARD",
  "cardNumber": "4111222233334444",
  "expiryMonth": "12",
  "expiryYear": "2026",
  "cvv": "123",
  "cardHolderName": "John Doe",
  "customerEmail": "customer@example.com"
}
```

## 📊 Observability

- **Grafana**: [http://localhost:3000](http://localhost:3000) (Explore -> Tempo for traces)
- **Prometheus**: [http://localhost:9090](http://localhost:9090) (Metrics)
- **RabbitMQ Management**: [http://localhost:15672](http://localhost:15672) (guest/guest)

## 🚀 **Read-Write Splitting (RWS)**
- **Architecture**:
    - **Primary (Write)**: Handles all `@Transactional` (read-write) operations.
    - **Replica (Read)**: Handles `@Transactional(readOnly = true)` operations.
- **Implementation**:
    - Uses `AbstractRoutingDataSource` with `LazyConnectionDataSourceProxy` for dynamic routing.
    - `DataSourceRoutingAspect` intercepts transactions to set the correct context (`PRIMARY` vs `SECONDARY`).
    - Configured in `payment-service` with two HikariCP data sources.

## 📊 **Performance Benchmarking**
A Python-based load testing script (`load_test.py`) is provided to benchmark the system.
- **Features**:
    - Simulates User Registration and Authentication.
    - Registers Merchants.
    - Generates concurrent Payment functionality load.
    - Measures Request Per Second (RPS) and Latency (P50, P95, P99).
- **Execution**:
    ```bash
    python load_test.py
    ```
- **Note**: Ensure all services are running via `docker-compose up -d` before running the benchmark. The script bypasses the API Gateway (port 8080) and hits services directly (8081, 8082, 8083) for isolated testing.

## License

Proprietary - All Rights Reserved
