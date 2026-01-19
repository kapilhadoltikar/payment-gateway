# High-Performance Distributed Payment Gateway

A state-of-the-art, secure, and scalable payment processing platform engineered for high-concurrency and ultra-low latency. Built with **Java 25**, **Spring Boot 4**, and optimized with **GraalVM Native Image**.

## 🏗️ Architecture

The system follows a clean, event-driven microservices architecture:

- **[API Gateway](./api-gateway)**: Entry point using Spring Cloud Gateway with reactive rate limiting.
- **[Auth Service](./auth-service)**: OAuth2 Authorization Server managing JWT lifecycles and security.
- **[Payment Service](./payment-service)**: Core transaction engine with Read-Write split persistence and Kafka event sourcing.
- **[Vault Service](./vault-service)**: PCI-DSS compliant vault using AES-256-GCM for sensitive data tokenization.
- **[Merchant Service](./merchant-service)**: Management of merchant onboarding and API credentials.
- **[Notification Service](./notification-service)**: Reliable webhook dispatcher backed by RabbitMQ.
- **[Fraud Service](./fraud-service)**: AI-driven fraud detection engine using **XGBoost** and **ONNX**.

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

## ⚡ Key Performance Optimizations

### 🚀 **GraalVM Native Image**
All services are optimized for native compilation:
- **Startup Time**: ~0.4s (45x faster than JVM).
- **Memory Footprint**: ~120MB per service (85% reduction).

### 🧵 **Virtual Threads (Project Loom)**
Highly optimized for I/O-bound operations. The system uses Java 25 `StructuredTaskScope` to handle concurrent I/O (Redis/Kafka) without blocking OS threads.

### 💾 **Read-Write Split (RWS)**
Database scalability via AOP-based routing:
- **Writes**: Routed to Primary PostgreSQL.
- **Reads**: Load-balanced across Replicas using `@Transactional(readOnly = true)`.

## 📊 Performance Results

| Metric | Standard JVM | GraalVM Native | Improvement |
|:---|:---|:---|:---|
| **Startup Time** | ~18.2s | **~0.4s** | **45x Faster** |
| **Memory Footprint** | ~800MB | **~120MB** | **85% Reduction** |
| **Peak Throughput** | ~120 RPS | **~180 RPS** | **50% Increase** |
| **P95 Latency** | ~45ms | **~12ms** | **73% Reduction** |

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
   ```bash
   go install github.com/go-task/task/v3/cmd/task@latest
   ```

### Running the Project
1. **Start Infrastructure**:
   ```bash
   docker-compose up -d
   ```

2. **Build and Run (JVM Mode)**:
   ```bash
   task build
   # Services will start on ports 8080-8086 (Fraud Service: 8086)
   ```

3. **Build and Run (Native Image Mode)**:
   ```bash
   task build:native
   # This will build Docker images with native binaries
   ```

4. **Verify Integration**:
   ```bash
   python verify_payment.py
   # Validates: Merchant Creation, Low Risk Payment, and High Risk Fraud Block
   ```

5. **Verify with Load Test**:
   ```bash
   python load_test.py
   ```

## 🔒 Security & Compliance
- **PCI-DSS Compliance**: Sensitive data is tokenized immediately at the edge via the Vault Service.
- **Zero Trust**: Inter-service communication is secured via JWT and strict security filters.
- **Encryption**: AES-256-GCM with automated key rotation.

---
*Proprietary - High-Performance Distributed Payment Gateway*
