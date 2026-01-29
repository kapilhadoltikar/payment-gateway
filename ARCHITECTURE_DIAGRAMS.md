# Payment Gateway - Complete Architecture Diagrams

## Available Visual Diagrams

We have created 5 professional diagrams (image generation quota reached):

1. **Payment Flow Diagram** - Shows complete transaction flow through all services
2. **System Architecture** - 3-layer architecture with all services and infrastructure
3. **Fraud Detection Flow** - ML pipeline in fraud-service:8086
4. **Deployment Architecture** - Docker Compose → CI/CD → Kubernetes
5. **Hybrid Runtime Strategy** - Native vs JVM service distribution

---

## Text-Based Architecture Diagrams

### 1. Payment Processing Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         PAYMENT PROCESSING FLOW                             │
└─────────────────────────────────────────────────────────────────────────────┘

CLIENT
  │ POST /api/payments
  │ {amount: 5000, card: "4532...", merchant: "STORE123"}
  ▼
┌──────────────────────────┐
│  api-gateway:8080        │  ✓ Rate Limiting
│  (GraalVM Native)        │  ✓ Request Validation
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│  auth-service:8081       │  ✓ JWT Validation
│  (GraalVM Native)        │  ✓ Extract User ID
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│  vault-service:8084      │  ✓ Tokenize Card: "4532..." → "tok_abc123"
│  (GraalVM Native)        │  ✓ Store in HashiCorp Vault
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│  fraud-service:8086      │  ✓ Extract 15+ Features
│  (JVM - XGBoost ML)      │  ✓ XGBoost Score: 0.85
│                          │  ✓ Decision: REVIEW
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│  payment-service:8082    │  ✓ Start Saga Transaction
│  (JVM - Saga Pattern)    │  ✓ Reserve Funds
│                          │  ✓ Write to payment-db:5434
│                          │  ✓ Publish to payment-mq:5673
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│  merchant-service:8083   │  ✓ Update Balance: +$5000
│  (GraalVM Native)        │  ✓ Settlement: PENDING
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│  notification-service    │  ✓ Webhook to Merchant
│  :8085 (GraalVM Native)  │  ✓ Email to Customer
│                          │  ✓ SMS Alert
└──────────────────────────┘

INFRASTRUCTURE USED:
  • payment-db:5434 (PostgreSQL 16) - Transaction storage
  • payment-mq:5673 (RabbitMQ) - Async event processing
  • postgres:5433 (PostgreSQL 16) - Main database
```

---

### 2. System Architecture (3-Layer)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│     Web App          Mobile App          API Clients (Third-Party)          │
└──────────────────────────────┬──────────────────────────────────────────────┘
                               │
┌──────────────────────────────▼─────────────────────────────────────────────┐
│                            SERVICE LAYER                                   │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  ┌─────────────────────────────────┐  ┌──────────────────────────────────┐ │
│  │  CONTROL PLANE (GraalVM Native) │  │    DATA PLANE (JVM Runtime)      │ │
│  ├─────────────────────────────────┤  ├──────────────────────────────────┤ │
│  │                                 │  │                                  │ │
│  │  • api-gateway:8080             │  │  • payment-service:8082          │ │
│  │    Routing, Rate Limiting       │  │    Saga Pattern, Transactions    │ │
│  │                                 │  │                                  │ │
│  │  • auth-service:8081            │  │  • fraud-service:8086            │ │
│  │    JWT Auth, OAuth2             │  │    XGBoost ML, Fraud Detection   │ │
│  │                                 │  │                                  │ │
│  │  • vault-service:8084           │  └──────────────────────────────────┘ │
│  │    Card Tokenization (PCI-DSS)  │                                       │
│  │                                 │    Startup: ~3s                       │
│  │  • merchant-service:8083        │    Memory: 200-250MB each             │
│  │    Merchant Profiles, KYC       │    Use: Complex logic, ML             │
│  │                                 │                                       │
│  │  • notification-service:8085    │                                       │
│  │    Webhooks, Email, SMS         │                                       │
│  │                                 │                                       │
│  └─────────────────────────────────┘                                       │
│                                                                            │
│    Startup: ~50ms                                                          │
│    Memory: ~50MB each                                                      │
│    Use: Request routing, simple ops                                        │
│                                                                            │
└──────────────────────────────┬─────────────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────────────┐
│                         INFRASTRUCTURE LAYER                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  DATABASES:                          MESSAGING:                             │
│  • postgres:5433                     • kafka:9092                           │
│    Main DB (PostgreSQL 16)             Event Streaming                      │
│                                                                             │
│  • payment-db:5434                   • rabbitmq:5672,15672                  │
│    Payment DB (PostgreSQL 16)          Message Queue                        │
│                                                                             │
│  • redis:6379                        • payment-mq:5673,15673                │
│    Cache (Redis 7)                     Payment Queue                        │
│                                                                             │
│  MONITORING:                                                                │
│  • prometheus:9090 - Metrics                                                │
│  • tempo:3200 - Distributed Tracing                                         │
│  • otel-collector - Telemetry Collection                                    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 3. Fraud Detection Pipeline (fraud-service:8086)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    fraud-service:8086 - AI/ML PIPELINE                      │
└─────────────────────────────────────────────────────────────────────────────┘

TRANSACTION INPUT
  │
  │ Amount: $5000
  │ Merchant: STORE123
  │ Card Token: tok_abc123
  │ Location: New York, US
  │ Time: 2026-01-29 15:00:00
  │
  ▼
┌──────────────────────────┐
│  FEATURE EXTRACTION      │
│                          │
│  Extracts 15+ features:  │
│  • Transaction amount    │
│  • Merchant category     │
│  • Geographic location   │
│  • Transaction velocity  │
│  • Historical patterns   │
│  • Device fingerprint    │
│  • Time of day           │
└────────────┬─────────────┘
             │
             ├─────────────┬─────────────┬─────────────┐
             │             │             │             │
             ▼             ▼             ▼             │
┌──────────────────┐ ┌──────────────┐ ┌─────────────┐  │
│  XGBoost Model   │ │  Logistic    │ │ Rule Engine │  │
│                  │ │  Regression  │ │             │  │
│  Champion Model  │ │  Challenger  │ │ Thresholds  │  │
│  95% Accuracy    │ │  92% Accuracy│ │ Blacklist   │  │
│                  │ │              │ │             │  │
│  Score: 0.85     │ │  Score: 0.78 │ │ Score: 0.90 │  │
└────────┬─────────┘ └──────┬───────┘ └──────┬──────┘  │
         │                  │                │         │
         └──────────────────┼────────────────┘         │
                            │                          │
                            ▼                          │
                  ┌──────────────────┐                 │
                  │ ENSEMBLE SCORER  │                 │
                  │                  │                 │
                  │ Formula:         │                 │
                  │ 0.6 × XGBoost +  │                 │
                  │ 0.3 × LogReg +   │                 │
                  │ 0.1 × Rules      │                 │
                  │                  │                 │
                  │ Final: 0.84      │                 │
                  └────────┬─────────┘                 │
                           │                           │
                           ▼                           │
                  ┌──────────────────┐                 │
                  │    DECISION      │                 │
                  │                  │                 │
                  │ ✅ APPROVED      │                 │
                  │    (< 0.3)       │                 │
                  │                  │                 │
                  │ ⚠️  REVIEW       │ ← SELECTED      │
                  │    (0.3 - 0.7)   │   (Score: 0.84) │
                  │                  │                 │
                  │ ❌ REJECTED      │                 │
                  │    (> 0.7)       │                 │
                  └────────┬─────────┘                 │
                           │                           │
                           ▼                           │
                  ┌──────────────────┐                 │
                  │   AUDIT LOG      │                 │
                  │                  │                 │
                  │ Saved to:        │                 │
                  │ payment-db:5434  │                 │
                  │                  │                 │
                  │ Includes:        │                 │
                  │ • Model scores   │                 │
                  │ • Feature values │                 │
                  │ • Decision       │                 │
                  │ • Timestamp      │                 │
                  └──────────────────┘                 │
```

---

### 4. Deployment Architecture

```
┌────────────────────────────────────────────────────────────────────────────┐
│                      DEVELOPMENT ENVIRONMENT                               │
│                         (Docker Compose)                                   │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  NATIVE SERVICES (5):                    JVM SERVICES (2):                 │
│  ┌─────────────────────┐                 ┌──────────────────────┐          │
│  │ api-gateway:8080    │                 │ payment-service:8082 │          │
│  │ auth-service:8081   │                 │ fraud-service:8086   │          │
│  │ vault-service:8084  │                 └──────────────────────┘          │
│  │ merchant-service    │                                                   │
│  │   :8083             │                 INFRASTRUCTURE (6):               │
│  │ notification-service│                 ┌──────────────────────┐          │
│  │   :8085             │                 │ postgres:5433        │          │
│  └─────────────────────┘                 │ payment-db:5434      │          │
│                                          │ redis:6379           │          │
│  Image: payment-gateway/*:native         │ kafka:9092           │          │
│  Build: mvn spring-boot:build-image      │ rabbitmq:5672        │          │
│         -Pnative                         │ payment-mq:5673      │          │
│                                          └──────────────────────┘          │
│                                                                            │
│  Command: task start  OR  task up:hybrid                                   │
│                                                                            │
└──────────────────────────────┬─────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CI/CD PIPELINE                                    │
│                          (GitHub Actions)                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. TEST                2. BUILD NATIVE       3. BUILD JVM      4. PUSH     │
│  ┌──────────┐          ┌──────────────┐      ┌───────────┐    ┌─────────┐   │
│  │ mvn test │  ──────▶ │ mvn spring-  │ ───▶ │ mvn       │ ─▶ │ Docker  │   │
│  │          │          │ boot:build-  │      │ spring-   │    │ Registry│   │
│  │ JUnit    │          │ image        │      │ boot:     │    │         │   │
│  │ Mockito  │          │ -Pnative     │      │ build-    │    │ Images  │   │
│  └──────────┘          │              │      │ image     │    │ Tagged  │   │
│                        │ Buildpacks   │      │           │    └─────────┘   │
│                        │ + GraalVM    │      │ Buildpacks│                  │
│                        └──────────────┘      └───────────┘                  │
│                                                                             │
└──────────────────────────────┬──────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      PRODUCTION ENVIRONMENT                                 │
│                          (Kubernetes)                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  NATIVE PODS (Auto-scale: 3-10)      JVM PODS (Auto-scale: 2-5)             │
│  ┌──────────────────────────┐        ┌──────────────────────────┐           │
│  │ api-gateway              │        │ payment-service          │           │
│  │ ├─ pod-1                 │        │ ├─ pod-1                 │           │
│  │ ├─ pod-2                 │        │ └─ pod-2                 │           │
│  │ └─ pod-3                 │        │                          │           │
│  │                          │        │ fraud-service            │           │
│  │ auth-service             │        │ ├─ pod-1                 │           │
│  │ vault-service            │        │ └─ pod-2                 │           │
│  │ merchant-service         │        └──────────────────────────┘           │
│  │ notification-service     │                                               │
│  └──────────────────────────┘        Resources:                             │
│                                       CPU: 1 core                           │
│  Resources:                           Memory: 512MB                         │
│  CPU: 0.25 core                       Always-on (warm JIT)                  │
│  Memory: 100MB                                                              │
│  Scale-to-zero capable                                                      │
│                                                                             │
│  PERSISTENT VOLUMES:                  MONITORING:                           │
│  • postgres-pv                        • prometheus:9090                     │
│  • payment-db-pv                      • tempo:3200                          │
│  • redis-pv                           • grafana:3000                        │
│  • kafka-pv                           • otel-collector                      │
│  • rabbitmq-pv                                                              │
│                                                                             │
│  LOAD BALANCER: Ingress Controller (NGINX)                                  │
│  DEPLOYMENT: Blue/Green with Rolling Updates                                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 5. Data Flow with Actual Values

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    DETAILED DATA FLOW EXAMPLE                               │
└─────────────────────────────────────────────────────────────────────────────┘

STEP 1: Client Request
────────────────────────────────────────────────────────────────────────────
POST http://localhost:8080/api/payments
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  Content-Type: application/json
Body:
  {
    "amount": 5000.00,
    "currency": "USD",
    "cardNumber": "4532015112830366",
    "cvv": "123",
    "expiryMonth": "12",
    "expiryYear": "2025",
    "merchantId": "STORE123"
  }

STEP 2: api-gateway:8080
────────────────────────────────────────────────────────────────────────────
✓ Rate Limit Check: 45/100 requests (PASS)
✓ Request Validation: Schema valid (PASS)
✓ Route Decision: → auth-service:8081

STEP 3: auth-service:8081
────────────────────────────────────────────────────────────────────────────
JWT Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
✓ Signature Valid: HMAC-SHA256 (PASS)
✓ Expiry Check: Valid until 2026-01-29 16:00:00 (PASS)
✓ User Extracted: user_12345
✓ Permissions: [PAYMENT_CREATE] (PASS)

STEP 4: vault-service:8084
────────────────────────────────────────────────────────────────────────────
Input Card: 4532015112830366
✓ Luhn Check: VALID
✓ Tokenize: 4532015112830366 → tok_abc123xyz789
✓ Store in HashiCorp Vault: /secret/cards/tok_abc123xyz789
✓ Return Masked: 4532********0366

STEP 5: fraud-service:8086
────────────────────────────────────────────────────────────────────────────
Features Extracted:
  • amount: 5000.00
  • merchant_category: RETAIL
  • location: New York, US (40.7128, -74.0060)
  • velocity_1h: 2 transactions
  • velocity_24h: 5 transactions
  • avg_transaction: 1200.00
  • time_of_day: 15:00 (afternoon)
  • device_fingerprint: fp_xyz789
  • is_new_merchant: false
  • distance_from_home: 5.2 km
  
Model Scores:
  • XGBoost: 0.85 (HIGH RISK)
  • Logistic Regression: 0.78 (MEDIUM RISK)
  • Rule Engine: 0.90 (HIGH RISK - Amount > $3000)
  
Ensemble Score: 0.6×0.85 + 0.3×0.78 + 0.1×0.90 = 0.84
Decision: REVIEW (0.3 < 0.84 < 0.7) → Manual review required

STEP 6: payment-service:8082
────────────────────────────────────────────────────────────────────────────
Saga Transaction Started: saga_txn_456789
✓ Reserve Funds: $5000.00 reserved from account
✓ Create Payment Record:
  INSERT INTO payments (
    id, user_id, merchant_id, amount, status, fraud_score, created_at
  ) VALUES (
    'pay_789', 'user_12345', 'STORE123', 5000.00, 'PENDING_REVIEW', 0.84, NOW()
  )
✓ Write to payment-db:5434 (PostgreSQL)
✓ Publish Event to payment-mq:5673:
  {
    "event": "payment.created",
    "paymentId": "pay_789",
    "status": "PENDING_REVIEW",
    "amount": 5000.00
  }

STEP 7: merchant-service:8083
────────────────────────────────────────────────────────────────────────────
✓ Update Merchant Balance:
  UPDATE merchants
  SET pending_balance = pending_balance + 5000.00
  WHERE merchant_id = 'STORE123'
  
✓ Settlement Status: PENDING (awaiting fraud review)
✓ Write to postgres:5433

STEP 8: notification-service:8085
────────────────────────────────────────────────────────────────────────────
✓ Webhook to Merchant:
  POST https://merchant.example.com/webhooks/payments
  {
    "paymentId": "pay_789",
    "status": "PENDING_REVIEW",
    "amount": 5000.00,
    "reason": "Fraud review required"
  }

✓ Email to Customer:
  To: customer@example.com
  Subject: Payment Under Review
  Body: Your payment of $5000.00 is being reviewed for security...

✓ SMS Alert:
  To: +1-555-0123
  Message: "Payment of $5000.00 received. Under security review."

────────────────────────────────────────────────────────────────────────────
FINAL RESPONSE TO CLIENT:
{
  "paymentId": "pay_789",
  "status": "PENDING_REVIEW",
  "amount": 5000.00,
  "message": "Payment is under security review",
  "estimatedReviewTime": "2-4 hours"
}
```

---

## Service Port Summary

```
SERVICE PORTS:
  8080 - api-gateway (Native)
  8081 - auth-service (Native)
  8082 - payment-service (JVM)
  8083 - merchant-service (Native)
  8084 - vault-service (Native)
  8085 - notification-service (Native)
  8086 - fraud-service (JVM)

INFRASTRUCTURE PORTS:
  5433 - postgres (Main DB)
  5434 - payment-db (Payment DB)
  6379 - redis (Cache)
  9092 - kafka (Events)
  5672, 15672 - rabbitmq (Messages + Management)
  5673, 15673 - payment-mq (Payment Queue + Management)
  9090 - prometheus (Metrics)
  3200 - tempo (Tracing)
  3000 - grafana (Dashboards)
```

---

## Quick Commands Reference

```bash
# Start everything
task start

# Build hybrid stack
task build:hybrid

# Run E2E tests
task test:python

# Access databases
task db-shell          # payment-db:5434
task db-shell:main     # postgres:5433

# View logs
task docker:logs

# Generate audit report
task audit
```

---

