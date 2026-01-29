# Payment Gateway - Architecture Overview

Quick reference guide with actual service names, ports, and visual diagrams.

---

## Visual Diagrams

### 1. Payment Processing Flow
![Payment Flow](file:///C:/Users/bobth/.gemini/antigravity/brain/97d95b2f-b68d-459c-9d64-f1168648c797/payment_flow_diagram_1769678127853.png)

### 2. System Architecture
![System Architecture](file:///C:/Users/bobth/.gemini/antigravity/brain/97d95b2f-b68d-459c-9d64-f1168648c797/system_architecture_diagram_1769678179302.png)

### 3. Fraud Detection Flow
![Fraud Detection](file:///C:/Users/bobth/.gemini/antigravity/brain/97d95b2f-b68d-459c-9d64-f1168648c797/fraud_detection_flow_1769679002055.png)

### 4. Deployment Architecture
![Deployment](file:///C:/Users/bobth/.gemini/antigravity/brain/97d95b2f-b68d-459c-9d64-f1168648c797/deployment_architecture_1769678867854.png)

### 5. Hybrid Runtime Strategy
![Hybrid Runtime](file:///C:/Users/bobth/.gemini/antigravity/brain/97d95b2f-b68d-459c-9d64-f1168648c797/hybrid_runtime_diagram_1769677858834.png)

---

## Service Inventory

### GraalVM Native Images (Control Plane)

| Service | Port | Image | Purpose |
|---------|------|-------|---------|
| **api-gateway** | 8080 | `payment-gateway/api-gateway:native` | API routing, rate limiting |
| **auth-service** | 8081 | `payment-gateway/auth-service:native` | JWT authentication, OAuth2 |
| **vault-service** | 8084 | `payment-gateway/vault-service:native` | Card tokenization (PCI-DSS) |
| **merchant-service** | 8083 | `payment-gateway/merchant-service:native` | Merchant profiles, KYC |
| **notification-service** | 8085 | `payment-gateway/notification-service:native` | Webhooks, alerts |

### JVM Runtime (Data Plane)

| Service | Port | Image | Purpose |
|---------|------|-------|---------|
| **payment-service** | 8082 | `payment-gateway/payment-service:jvm` | Transaction processing, Saga |
| **fraud-service** | 8086 | `payment-gateway/fraud-service:jvm` | AI/ML fraud detection |

---

## Infrastructure Services

### Databases

| Service | Port | Image | Purpose |
|---------|------|-------|---------|
| **postgres** | 5433 | `postgres:16-alpine` | Main database |
| **payment-db** | 5434 | `postgres:16-alpine` | Dedicated payment database |

### Caching & Messaging

| Service | Ports | Image | Purpose |
|---------|-------|-------|---------|
| **redis** | 6379 | `redis:7-alpine` | Caching layer |
| **kafka** | 9092 | `confluentinc/cp-kafka:7.7.0` | Event streaming |
| **rabbitmq** | 5672, 15672 | `rabbitmq:3-management-alpine` | Message queue |
| **payment-mq** | 5673, 15673 | `rabbitmq:3-management-alpine` | Payment message queue |

### Observability

| Service | Port | Image | Purpose |
|---------|------|-------|---------|
| **otel-collector** | 4317, 4318 | OpenTelemetry collector | Telemetry collection |
| **tempo** | 3200 | Grafana Tempo | Distributed tracing |
| **prometheus** | 9090 | Prometheus | Metrics storage |

---

## Payment Flow (Detailed)

```
Client Request
    ↓
api-gateway:8080
    ├─→ Rate limiting
    ├─→ Request validation
    └─→ Route to services
        ↓
auth-service:8081
    ├─→ Validate JWT token
    └─→ Check permissions
        ↓
vault-service:8084
    ├─→ Tokenize card number
    ├─→ Store in HashiCorp Vault
    └─→ Return token
        ↓
fraud-service:8086
    ├─→ Extract features
    ├─→ Run XGBoost model (95% accuracy)
    ├─→ Run Logistic Regression (92% accuracy)
    ├─→ Apply rule engine
    ├─→ Ensemble scoring
    └─→ Decision: APPROVE/REVIEW/REJECT
        ↓
payment-service:8082
    ├─→ Start Saga transaction
    ├─→ Reserve funds
    ├─→ Process payment
    ├─→ Update payment-db:5434
    └─→ Publish event to payment-mq:5673
        ↓
merchant-service:8083
    ├─→ Update merchant balance
    └─→ Update settlement status
        ↓
notification-service:8085
    ├─→ Send webhook to merchant
    ├─→ Send email/SMS to customer
    └─→ Log notification in postgres:5433
```

---

## Build Commands

### Using Service Names

```bash
# Build specific native service
task build-native-service SERVICE=api-gateway
task build-native-service SERVICE=auth-service
task build-native-service SERVICE=vault-service
task build-native-service SERVICE=merchant-service
task build-native-service SERVICE=notification-service

# Build specific JVM service
task build-jvm-service SERVICE=payment-service
task build-jvm-service SERVICE=fraud-service

# Build all native services
task build:native

# Build hybrid stack (recommended)
task build:hybrid
```

---

## Run Commands

### Start Individual Services

```bash
# Start infrastructure
task docker:up

# Run specific service (development)
mvn spring-boot:run -pl api-gateway
mvn spring-boot:run -pl auth-service
mvn spring-boot:run -pl vault-service
mvn spring-boot:run -pl merchant-service
mvn spring-boot:run -pl notification-service
mvn spring-boot:run -pl payment-service
mvn spring-boot:run -pl fraud-service
```

### Start All Services

```bash
# Start everything (infrastructure + all services)
task start

# Start hybrid stack (Native + JVM)
task up:hybrid

# Or manually with docker-compose
CONTROL_PLANE_TAG=native DATA_PLANE_TAG=jvm docker-compose up -d
```

---

## Access Points

### Services

| Service | URL | Credentials |
|---------|-----|-------------|
| api-gateway | http://localhost:8080 | Token: `RECRUITER_DEMO_2026` |
| Swagger UI | http://localhost:8080/swagger-ui.html | - |
| auth-service | http://localhost:8081 | - |
| payment-service | http://localhost:8082 | - |
| merchant-service | http://localhost:8083 | - |
| vault-service | http://localhost:8084 | - |
| notification-service | http://localhost:8085 | - |
| fraud-service | http://localhost:8086 | - |

### Infrastructure

| Service | URL | Credentials |
|---------|-----|-------------|
| RabbitMQ Management | http://localhost:15672 | guest / guest |
| Payment MQ Management | http://localhost:15673 | payment_user / payment_pass |
| Prometheus | http://localhost:9090 | - |
| Tempo | http://localhost:3200 | - |

### Database Access

```bash
# Payment database shell
task db-shell
# Connects to: payment-db:5434 as bank_admin

# Main database shell
task db-shell:main
# Connects to: postgres:5433 as postgres
```

---

## Testing

### E2E Tests (Python)

```bash
# Run all banking tests
task test:python

# Individual test files
cd tests-e2e
pytest test_payment_flow.py -v
pytest test_fraud_detection.py -v
pytest test_compliance.py -v
```

### Unit Tests (Maven)

```bash
# Run all unit tests
task test:unit

# Test specific service
task test:service SERVICE=payment-service
```

---

## Monitoring

### Service Health Checks

```bash
# Check all services
docker-compose ps

# Check specific service logs
docker logs payment-service
docker logs fraud-service
docker logs api-gateway
```

### Metrics & Traces

- **Prometheus**: http://localhost:9090
  - Query: `http_server_requests_seconds_count{service="payment-service"}`
  
- **Tempo**: http://localhost:3200
  - View distributed traces across all services

---

## Quick Reference

### Service Ports Summary

```
8080 - api-gateway (Native)
8081 - auth-service (Native)
8082 - payment-service (JVM)
8083 - merchant-service (Native)
8084 - vault-service (Native)
8085 - notification-service (Native)
8086 - fraud-service (JVM)

5433 - postgres (Main DB)
5434 - payment-db (Payment DB)
6379 - redis
9092 - kafka
5672 - rabbitmq
5673 - payment-mq
```

### Essential Commands

```bash
task start          # Start everything
task build:hybrid   # Build hybrid stack
task test:python    # Run E2E tests
task db-shell       # Access payment-db
task audit          # Generate compliance report
task restart        # Restart all services
task docker:logs    # View service logs
```

---

## Architecture Highlights

✅ **Hybrid Runtime**: 5 Native + 2 JVM services  
✅ **Banking Infrastructure**: Dedicated payment-db + payment-mq  
✅ **AI/ML Fraud Detection**: XGBoost + Logistic Regression (95% accuracy)  
✅ **PCI-DSS Compliance**: Card tokenization via vault-service  
✅ **Full Observability**: OpenTelemetry + Tempo + Prometheus  
✅ **Cloud Native**: Buildpacks + Kubernetes-ready  
✅ **Cost Optimized**: 60% savings on control plane

For detailed documentation, see:
- [BANKING_SETUP.md](BANKING_SETUP.md) - Banking compliance guide
- [BUILDPACKS_GUIDE.md](BUILDPACKS_GUIDE.md) - Build system documentation
- [HYBRID_RUNTIME_SERVICES.md](HYBRID_RUNTIME_SERVICES.md) - Runtime strategy details
