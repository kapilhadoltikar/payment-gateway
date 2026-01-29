# Test Strategy Guide

## 1. Test Pyramid

We follow the standard banking-grade test pyramid:

| Layer | Scope | Framework | Suffix |
|-------|-------|-----------|--------|
| **Unit** | Single Class/Method | JUnit 5 + Mockito | `*Test.java` |
| **Integration** | Service + DB/Kafka | Testcontainers | `*IT.java` |
| **Smoke** | Deployed Service Validity | Kubernetes Job | `smoke-test-job.yaml` |
| **E2E** | Full System Flow | Python Pytest | `verify_payment.py` |

## 2. Unit Tests
- **Goal**: Verify business logic in isolation.
- **Execution**: `./mvnw test`
- **Coverage Target**: > 80% line coverage.

## 3. Integration Tests
- **Goal**: Verify component interaction with real infrastructure (Postgres, Kafka).
- **Tooling**: Testcontainers spins up disposable Docker containers.
- **Execution**: `./mvnw verify`

## 4. Smoke Tests (Post-Deployment)
- **Goal**: Verify services are running and healthy in the cluster.
- **Command**: `task test:smoke`
- **Actions**: Checks `/actuator/health` of all services.

## 5. End-to-End Banking Suite
- **Goal**: Verify full payment lifecycle and compliance rules.
- **Location**: `tests-e2e/`
- **Execution**: `task test:python`
