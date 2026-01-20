# High-Performance Distributed Payment Gateway

> **Hybrid Runtime Architecture**: Strategic deployment using GraalVM Native for Control Plane (instant scaling) and Standard JVM for Data Plane (peak throughput)

A state-of-the-art, secure, and scalable payment processing platform engineered for high-concurrency and ultra-low latency. Built with **Java 25**, **Spring Boot 4**, and optimized with a **Hybrid GraalVM Native/JVM Strategy** that balances infrastructure cost with system performance.

## 🏗️ Hybrid Runtime Architecture

The system follows a clean, event-driven microservices architecture with **strategic runtime allocation** based on workload characteristics:

### Control Plane (GraalVM Native) - Instant Scaling
- **[API Gateway](./api-gateway)** - Entry point with reactive rate limiting | *0.4s startup, 120MB memory*
- **[Auth Service](./auth-service)** - OAuth2 Authorization Server | *0.3s startup, 110MB memory*
- **[Vault Service](./vault-service)** - PCI-DSS compliant tokenization | *0.35s startup, 105MB memory*
- **[Merchant Service](./merchant-service)** - CRUD operations | *0.4s startup, 125MB memory*
- **[Notification Service](./notification-service)** - RabbitMQ webhook dispatcher | *0.45s startup, 130MB memory*

### Data Plane (Standard JVM) - Peak Throughput
- **[Payment Service](./payment-service)** - Transaction engine with Event Sourcing | *180 RPS sustained*
- **[Fraud Service](./fraud-service)** - AI-driven XGBoost/ONNX inference | *120 RPS sustained*

**Why Hybrid?** See [HYBRID_RUNTIME_STRATEGY.md](./HYBRID_RUNTIME_STRATEGY.md) for detailed technical justifications.

## 🧠 AI-Driven Fraud Detection

The **Fraud Service** serves as the brain of the decision engine, transforming raw transaction data into a risk score via a multi-layered pipeline.

### 1. Real-Time Logic Flow ("Filter-Score-Decide")
To ensure sub-millisecond responses, the logic follows a specialized pipeline:
- **Ingestion**: Collects raw data (Amount, Merchant ID, User ID, Device Fingerprint, IP).
- **Feature Enrichment**: The "Feature Store" (Redis) fetches historical velocity and behavioral data.
- **XGBoost Inference**: The enriched vector is sent to the ONNX model, outputting a probability score ($0.0 \to 1.0$).
- **Decision Policy**:
    - **Score < 0.3**: **Green Path** (Auto-Approve).
    - **Score 0.3 – 0.8**: **Gray Path** (Trigger 3D Secure/MFA).
    - **Score > 0.8**: **Red Path** (Hard Block & Flag for Review).

### 2. Feature Engineering ("The Secret Sauce")
The model calculates high-impact features in real-time using Redis:
| Category | Examples | Purpose |
| :--- | :--- | :--- |
| **Identity** | `is_new_device`, `email_risk` | Detects account takeovers. |
| **Velocity** | `tx_count_1h`, `unique_merch_24h` | Detects "card testing" or rapid-fire fraud. |
| **Behavioral** | `avg_ticket_delta`, `is_night_tx` | Identifies deviations from normal habits. |
| **Network** | `ip_proxy_detected`, `zip_ip_match` | Detects IP masking or location anomalies. |

### 3. Handling the "Cold Start"
For new users with no history:
- **Rule-Based Fallback**: Applies stricter traditional rules (e.g., limit single TX to $200).
- **Global Features**: leverages global merchant risk scores instead of user-specific ones.

### 4. Continuous Learning (Feedback Loop)
- **Labeling**: When a merchant flags a transaction as a "Chargeback", the event is published to the `chargeback-events` Kafka topic.
- **Ingestion**: The `FraudFeedbackListener` consumes these events to label the original transaction data.
- **Retraining**: A background process re-trains the XGBoost model with these new labels.
- **Deployment**: Updated `.onnx` models are redeployed via the automated `Task` pipeline.

## 🛠️ Technology Stack

| Layer | Technologies |
|:---|:---|
| **Core** | Java 25 (Virtual Threads), Spring Boot 4.0.1, Spring Cloud |
| **Optimization** | GraalVM Native Image, PGO, CRaC (Checkpoint/Restore) |
| **Data** | PostgreSQL 16 (RWS), Redis 7 (Reactive), Apache Kafka 3.9 |
| **Messaging** | RabbitMQ 3.13 (with DLQ), Kafka Event Sourcing |
| **Security** | OAuth2, JWT, AES-256-GCM, Spring Security 7 |
| **AI/ML** | XGBoost, ONNX Runtime, Java 25 StructuredTaskScope |
| **Observability** | OpenTelemetry, Grafana Tempo, Prometheus, Grafana |

## 🛡️ Resilience & Reliability
- **Idempotency**: `PaymentService` ensures exactly-once processing using unique `idempotency-key` tracking.
- **Rate Limiting**: `FraudService` enforces strict velocity checks (Token Bucket) to prevent rapid-fire attacks.
- **Circuit Breakers**: Graceful degradation when dependent services (Merchant, Vault) are unavailable.
- **Unit Testing**: Comprehensive JUnit 5 test suite covering 90% of business logic.

## ⚡ Key Performance Optimizations

### 🎯 **Hybrid Runtime Strategy**
Strategic runtime allocation based on workload characteristics:
- **Control Plane (Native)**: API Gateway, Auth, Vault, Merchant, Notification → Instant scaling, minimal memory
- **Data Plane (JVM)**: Payment, Fraud → Peak throughput for computational workloads

### 🚀 **GraalVM Native Image (Control Plane)**
Control Plane services achieve dramatic improvements:
- **Startup Time**: ~0.4s (45x faster than JVM)
- **Memory Footprint**: ~120MB per service (85% reduction)
- **Cloud Cost**: 38% reduction via pod density & scale-to-zero

### 🧵 **Virtual Threads (Project Loom)**
Highly optimized for I/O-bound operations across both runtimes. The system uses Java 25 `StructuredTaskScope` to handle concurrent I/O (Redis/Kafka) without blocking OS threads.

### 💾 **Read-Write Split (RWS)**
Database scalability via AOP-based routing:
- **Writes**: Routed to Primary PostgreSQL.
- **Reads**: Load-balanced across Replicas using `@Transactional(readOnly = true)`.

### 🔥 **JIT Optimization (Data Plane)**
Payment and Fraud services leverage JVM runtime profiling:
- **Adaptive Compilation**: C2 compiler optimizes hot paths for 26% higher throughput
- **Mathematical Performance**: Vectorization and SIMD instructions accelerate XGBoost inference


## 📊 Performance Results

### Hybrid Runtime Comparison

| Service Type | Runtime | Startup Time | Memory Footprint | Throughput | Use Case |
|:-------------|:--------|:-------------|:-----------------|:-----------|:---------|
| **Control Plane** | GraalVM Native | **0.4s** | **120MB** | 280 RPS | Instant scaling for traffic bursts |
| **Data Plane** | Standard JVM | 18s | 800MB | **180 RPS** | Sustained computational workloads |

### Key Improvements (Native Services)

| Metric | Standard JVM | GraalVM Native | Improvement |
|:-------|:-------------|:---------------|:------------|
| **Startup Time** | ~18.2s | **~0.4s** | **45x Faster** |
| **Memory Footprint** | ~800MB | **~120MB** | **85% Reduction** |
| **Cloud Cost** | Baseline | **38% Lower** | Via pod density & scale-to-zero |
| **P95 Latency** | ~45ms | **~12ms** | **73% Reduction** (routing layer) |

**System-Wide**: 35+ RPS sustained throughput | 46ms P95 latency | 0% error rate @ 1000+ requests

## 🚀 Getting Started

### Prerequisites
- **Java 25** (OpenJDK or GraalVM)
- **Docker & Docker Compose**
- **Go** (for Task installation)
- **Python 3.10+** (for load testing)

### Installation
1. **Clone the repository**:
   ```bash
   git clone <repo-url>
   cd payment-gateway
   ```

2. **Install Task**:
   - **Windows**: `choco install go-task`
   - **Linux/Mac**: `sh -c "$(curl --location https://taskfile.dev/install.sh)" -- -d`
   - **Alternative**: `go install github.com/go-task/task/v3/cmd/task@latest`

### Running the Project
1. **Start Infrastructure**:
   ```bash
   task infra:up
   # Starts PostgreSQL, Redis, Kafka, RabbitMQ, etc.
   ```

2. **Build and Run (Hybrid Mode - Recommended)**:
   ```bash
   task build:hybrid
   # Builds Native binaries for Control Plane and JVM images for Data Plane
   
   task up:hybrid
   # Starts the entire hybrid stack via Docker Compose
   ```

3. **Build Native Control Plane Only**:
   ```bash
   task build:native
   # This will build Docker images with native binaries for Control Plane services
   ```

4. **Verify Integration**:
   ```bash
   task test:e2e
   # Platform-agnostic verification of: Merchant Creation, Clean Payment, and Fraud Block
   ```

5. **Run Load Tests**:
   ```bash
   python load_test.py
   # Tests system-wide performance: 35+ RPS, 46ms P95 latency
   ```

## 🔒 Security & Compliance
- **PCI-DSS Compliance**: Sensitive data is tokenized immediately at the edge via the Vault Service.
- **Zero Trust**: Inter-service communication is secured via JWT and strict security filters.
- **Encryption**: AES-256-GCM with automated key rotation.

---
*Proprietary - High-Performance Distributed Payment Gateway*
