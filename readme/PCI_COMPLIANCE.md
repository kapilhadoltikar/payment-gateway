# PCI-DSS Compliance Checklist

## 1. Requirement 1: Firewall Configuration
- [x] **Network Policies**: Implemented ingress/egress controls (`k8s/network-policy-*.yaml`).
- [x] **DMZ**: API Gateway acts as the only entry point; database is isolated.

## 2. Requirement 3: Protect Stored Data
- [x] **Vault Service**: Secure Cardholder Data (CHD) storage.
- [x] **Encryption**: AES-256-GCM used for stored data.
- [ ] **Data Retention**: Ensure purge policies are active.

## 3. Requirement 4: Encrypt Transmission
- [x] **TLS 1.2+**: Enforced at Ingress (`k8s/ingress-tls.yaml`).
- [x] **HSTS**: `Strict-Transport-Security` header enabled.
- [ ] **mTLS**: Internal service-to-service encryption (Optional/Future).

## 4. Requirement 6: Secure Systems
- [x] **Updates**: automated build pipeline uses latest distroless images.
- [x] **Vulnerability Scanning**: CI pipeline scans for CVEs.

## 5. Requirement 10: Monitoring
- [x] **Audit Trails**: Kafka event sourcing logs all transactions.
- [x] **Tracing**: OpenTelemetry tracks request flow.
- [x] **Access Logs**: Nginx and Service logs retained.

## Verification Procedures

### TLS Verification
Run:
```bash
openssl s_client -connect <host>:443 -tls1_2
```
Expected: Connection Established.

### Network Isolation Verification
Try to access database directly from a non-authorized pod:
```bash
kubectl run -it --rm test-pod --image=curlimages/curl -- curl postgres:5432
```
Expected: Timeout / Connection Refused.

### Access Control
Verify Actuator endpoints are secured:
```bash
curl http://payment-service:8082/actuator/env
```
Expected: 401 Unauthorized or 404 Not Found (if disabled).
