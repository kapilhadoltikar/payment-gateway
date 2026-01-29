# Banking-Grade Payment Gateway Setup

## Overview

This payment gateway is designed to meet **banking-sector compliance standards** including PCI-DSS, SOC2, and financial audit requirements. The architecture follows a multi-module Maven structure with complete separation of concerns:

- **Maven** handles code compilation and dependency management
- **Docker** manages infrastructure (databases, message queues)
- **Taskfile** provides developer-friendly workflow commands

## Quick Start for Banking Teams

### Prerequisites

- Java 21+
- Docker & Docker Compose
- [Task](https://taskfile.dev/) (install: `choco install go-task` on Windows)
- Python 3.9+ (for E2E tests)

### Start the Entire Stack

```bash
task start
```

This single command will:
1. Start all infrastructure (PostgreSQL, Redis, Kafka, RabbitMQ, payment-db, payment-mq)
2. Build all microservices (hybrid Native/JVM)
3. Launch all services
4. Display access URLs

**Access Points:**
- API Gateway: http://localhost:8080
- Payment Database: `task db-shell`
- RabbitMQ Management: http://localhost:15673

### Run Tests

```bash
# Maven unit & integration tests
task test:unit

# Python E2E functional tests
task test:python

# Full verification suite
task verify
```

## Architecture

### Multi-Module Maven Structure

```
payment-gateway/
├── pom.xml                    # Parent POM with dependency management
├── common/                    # Shared DTOs, utilities
├── api-gateway/              # API Gateway (Native)
├── auth-service/             # Authentication (Native)
├── payment-service/          # Payment processing (JVM)
├── merchant-service/         # Merchant management (Native)
├── vault-service/            # Card tokenization (Native)
├── notification-service/     # Notifications (Native)
└── fraud-service/            # Fraud detection (JVM)
```

### Banking Infrastructure

#### Dedicated Payment Database (`payment-db`)
- **Purpose**: Transaction isolation for payment data
- **Credentials**: `bank_admin` / `secure_password` (use secrets manager in production)
- **Port**: 5434
- **Access**: `task db-shell`

#### Payment Message Queue (`payment-mq`)
- **Purpose**: Async payment processing (PCI-DSS requirement)
- **Type**: RabbitMQ
- **Ports**: 5673 (AMQP), 15673 (Management UI)
- **Credentials**: `payment_user` / `payment_pass`

## Developer Workflow

### Essential Commands

| Command | Description |
|---------|-------------|
| `task start` | Start everything (infrastructure + services) |
| `task build` | Build all Java modules |
| `task test:unit` | Run Maven tests |
| `task test:python` | Run E2E banking tests |
| `task db-shell` | Open payment database shell |
| `task docker:up:banking` | Start only banking infrastructure |
| `task audit` | Generate compliance audit report |
| `task restart` | Restart all services |

# Banking-Grade Setup Guide

## 1. Build System Standards
- **Maven Wrapper**: Use `./mvnw` (Linux/Mac) or `.\mvnw.cmd` (Windows) for all builds to ensure Maven 3.9.6 consistency.
- **Java 21 LTS**: All services run on Java 21 for long-term stability.

## 2. Kubernetes Production Features
- **Auto-Scaling**: HorizontalPodAutoscaler (HPA) configured for 2-10 replicas.
- **Health Checks**: Liveness and Readiness probes enabled via Spring Actuator.
- **TLS 1.2+**: Enforced at Ingress level with cert-manager.

## 3. Compliance Verification
Run the following audit commands before deployment:
```bash
# 1. Generate Dependency Audit
task audit

# 2. verify PCI-DSS Network Policies
kubectl get networkpolicies

# 3. Verify TLS Enforcement
openssl s_client -connect payment-gateway.local:443 -tls1_2
```

## 4. Deployment Procedures
1. **Infrastructure**: `task docker:up:banking`
2. **Build**: `task build:hybrid`
3. **Deploy (Local)**: `task up:hybrid`
4. **Smoke Test**: `task test:smoke`
5. **E2E Verification**: `task test:python`

For detailed guides:
- [PCI Compliance Checklist](PCI_COMPLIANCE.md)
- [Kubernetes Production Guide](KUBERNETES_PRODUCTION.md)
- [Test Strategy](TEST_STRATEGY.md)

### Development Cycle

1. **Make code changes** in your service module
2. **Build**: `task build-service SERVICE=payment-service`
3. **Test**: `task test:service SERVICE=payment-service`
4. **Run**: `task run:service SERVICE=payment-service`

### Database Access

```bash
# Payment database (banking transactions)
task db-shell

# Main database (general data)
task db-shell:main
```

## Testing Infrastructure

### Python E2E Test Suite (`tests-e2e/`)

Comprehensive banking-grade functional tests:

- **Payment Flow Tests**: Success scenarios, validation, idempotency
- **Fraud Detection Tests**: High-amount triggers, rapid transactions
- **Compliance Tests**: Audit trails, PCI-DSS card masking, transaction immutability

**Run tests:**
```bash
cd tests-e2e
pip install -r requirements.txt
pytest -v
```

Or use Taskfile:
```bash
task test:python
```

### Maven Integration Tests

Uses **Testcontainers** for database and message queue testing:

```bash
task test:unit
```

## Banking Compliance Features

### 1. Audit Trail
Every transaction creates an immutable audit record with:
- Timestamp
- Merchant ID
- Transaction details
- Status changes
- Fraud scores

**Verify**: `task db-shell` → `SELECT * FROM audit_log;`

### 2. PCI-DSS Compliance
- Card numbers are **never** stored in full
- CVV is **never** persisted
- All card data is tokenized via `vault-service`
- API responses mask sensitive data

**Test**: `task test:python` runs PCI-DSS validation tests

### 3. Transaction Immutability
Completed transactions **cannot be modified** (audit requirement). Any attempt to update a transaction is rejected.

**Test**: See `tests-e2e/test_compliance.py::test_transaction_immutability`

### 4. Dependency Auditability
Generate complete dependency tree for security audits:

```bash
task audit
```

Output: `audit/dependency-tree.txt`, `audit/dependency-list.txt`

## CI/CD Integration

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'task build'
            }
        }
        stage('Test') {
            steps {
                sh 'task test:unit'
            }
        }
        stage('E2E Tests') {
            steps {
                sh 'task start'
                sh 'task test:python'
            }
        }
        stage('Audit') {
            steps {
                sh 'task audit'
                archiveArtifacts 'audit/*.txt'
            }
        }
    }
}
```

### GitHub Actions Example

```yaml
name: Banking Gateway CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Install Task
        run: sh -c "$(curl --location https://taskfile.dev/install.sh)" -- -d -b /usr/local/bin
      - name: Build
        run: task build
      - name: Unit Tests
        run: task test:unit
      - name: E2E Tests
        run: |
          task start
          task test:python
      - name: Audit
        run: task audit
```

## Security Considerations

### Production Deployment

1. **Secrets Management**: Replace hardcoded credentials with:
   - HashiCorp Vault
   - AWS Secrets Manager
   - Azure Key Vault

2. **Database Encryption**: Enable encryption at rest for `payment-db`

3. **Network Isolation**: Deploy services in private subnets

4. **TLS/SSL**: Enable HTTPS for all API endpoints

5. **Rate Limiting**: Configure rate limits in `api-gateway`

### Environment Variables

Create `.env` file (see `.env.example`):

```bash
# Database
PAYMENT_DB_URL=jdbc:postgresql://payment-db:5432/payment_db
PAYMENT_DB_USER=bank_admin
PAYMENT_DB_PASSWORD=<use-secrets-manager>

# Message Queue
PAYMENT_MQ_HOST=payment-mq
PAYMENT_MQ_USER=payment_user
PAYMENT_MQ_PASSWORD=<use-secrets-manager>
```

## Troubleshooting

### Services won't start

```bash
# Check infrastructure health
docker-compose ps

# View logs
task docker:logs

# Restart everything
task restart
```

### Database connection issues

```bash
# Verify payment-db is running
docker exec -it payment-gateway-payment-db pg_isready -U bank_admin

# Check connection from service
task db-shell
```

### Test failures

```bash
# Ensure services are running
task start

# Wait for health checks
sleep 30

# Run tests with verbose output
cd tests-e2e
pytest -v --tb=long
```

## Performance Optimization

### Hybrid Runtime Strategy

This gateway uses a **hybrid Native/JVM approach**:

- **Native Images** (Control Plane): api-gateway, auth-service, merchant-service, vault-service, notification-service
  - Fast startup (~50ms)
  - Low memory footprint (~50MB)
  - Ideal for request routing and simple operations

- **JVM** (Data Plane): payment-service, fraud-service
  - Better for ML models (fraud detection)
  - Optimized for throughput
  - Supports dynamic features

**Build hybrid stack:**
```bash
task build:hybrid
task up:hybrid
```

## Support & Documentation

- **Project Guide**: See `PROJECT_GUIDE.md`
- **Hybrid Runtime**: See `HYBRID_RUNTIME_STRATEGY.md`
- **Security**: See `SECURITY.md`
- **API Docs**: http://localhost:8080/swagger-ui.html (when running)

## Compliance Certifications

This architecture supports:
- ✅ PCI-DSS Level 1
- ✅ SOC 2 Type II
- ✅ ISO 27001
- ✅ GDPR (data retention policies)

**Audit Reports**: Generated via `task audit`
