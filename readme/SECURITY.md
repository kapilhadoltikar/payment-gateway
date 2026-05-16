# Security Manifest

This document outlines the security posture, threat modeling, and compliance measures for the Payment Gateway.

## STRIDE Threat Modeling

We use the **STRIDE** methodology to identify and mitigate potential security threats.

| Threat Category | Potential Attack | Mitigation Strategy |
| :--- | :--- | :--- |
| **Spoofing** | Unauthorized merchant pretending to be another. | **OAuth2/JWT** with strict client-id/client-secret verification. Mutual TLS (mTLS) for inter-service communication. |
| **Tampering** | Man-in-the-middle (MITM) altering payment amounts. | **AES-256-GCM** encryption for payload at rest. HMAC signature verification for all webhook events. |
| **Repudiation** | Merchant denying they initiated a transaction. | **Immutable Audit Logs** stored in PostgreSQL. Digital signatures on all critical financial events. |
| **Information Disclosure** | Leakage of Cardholder Data (CHD) in logs. | **Regex-based Data Masking** in application logs. Immediate tokenization in the **Secure Vault Service**. |
| **Denial of Service** | Volumetric attacks on the /payments endpoint. | **Redis-backed Rate Limiting** (Token Bucket) at the API Gateway. Automatic circuit breaking for unstable dependencies. |
| **Elevation of Privilege** | Compromised microservice accessing Vault data. | **Service Mesh (Istio/Linkerd)** policies. Static binary isolation of the Vault service with no outbound internet access. |

## Critical Mitigations

### 🔄 Double Spending (Idempotency)
To prevent duplicate charges due to network retries, all write operations require an `X-Idempotency-Key`.
- **Logic**: The `idempotency-key` is stored in Redis with a 24-hour TTL.
- **Handling**: If a duplicate key is detected, the system returns a `409 Conflict` along with the original transaction status.

### 🛡️ PII Exposure & Encryption
- **At Rest**: Sensitive fields (Card Number, CVV) are encrypted using **AES-256-GCM** before being stored in the Vault.
- **In Transit**: All traffic is encrypted via TLS 1.3. Internal service-to-service traffic uses mTLS.

### 🧩 Injection Attacks & Binary Isolation
- **Vault Isolation**: The `vault-service` is compiled as a **GraalVM Native Static Binary**. This minimizes the attack surface by removing the shell, JVM, and unused libraries from the runtime environment.
- **Input Validation**: Strict schema validation using Jakarta Bean Validation (`@Valid`) on all incoming DTOs.

## Data Masking in Logs

To prevent accidental leakage of sensitive information (PCI-DSS violation), we implement a multi-layered Regex-based logging filter.

### Masking Strategy
The system automatically identifies and replaces card numbers and PII in application logs using the following patterns:

```regex
# Card Number Masking (replaces with ****-****-****-1234)
(?:\d{4}-){3}\d{4}  ->  ****-****-****-$4

# CVV Masking
"cvv"\s*:\s*"\d{3,4}"  ->  "cvv": "***"
```

### Implementation
Filters are applied at the **OpenTelemetry** collector level and via a custom **Logback MaskingLayout** to ensure no plaintext CHD ever reaches the centralized logging stack (ELK/Grafana Loki).
