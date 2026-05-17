# Kubernetes Production Guide

## 1. Auto-Scaling (HPA)

Horizontal Pod Autoscaler (HPA) is configured for critical services to handle Black Friday traffic (2-10 replicas).

### Scaling Triggers
- **CPU Utilization**: Services scale up when average CPU exceeds target (70-80%).
- **Memory Utilization**: Additional safety trigger on memory usage (80-85%).

### Configuration
Apply HPA manifests:
```bash
kubectl apply -f k8s/hpa-api-gateway.yaml
kubectl apply -f k8s/hpa-payment-service.yaml
kubectl apply -f k8s/hpa-fraud-service.yaml
```

## 2. Health Checks (Probes)

Spring Boot Actuator is used for Liveness and Readiness probes.

| Probe | Path | Purpose | Behavior |
|-------|------|---------|----------|
| **Liveness** | `/actuator/health/liveness` | Detect deadlocks/crashes | Kills pod if failed |
| **Readiness** | `/actuator/health/readiness` | Verify traffic acceptance | Removes from Service IP if failed |

**Tuning Guidelines**:
- `initialDelaySeconds`: Set to 10s for JVM, 1s for Native Image to account for startup difference.
- `failureThreshold`: Set to 3 to avoid flapping.

## 3. TLS Configuration (PCI-DSS)

TLS 1.2+ is enforced at the Ingress level.

### Certificate Management
Cert-manager handles automated certificate issuance/renewal via Let's Encrypt.

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    nginx.ingress.kubernetes.io/ssl-protocols: "TLSv1.2 TLSv1.3"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
```

### Verification
Test TLS version enforcement:
```bash
openssl s_client -connect payment-gateway.local:443 -tls1_2 # Should succeed
openssl s_client -connect payment-gateway.local:443 -tls1_1 # Should fail
```

## 4. Network Policies

Zero Trust network model is implemented using NetworkPolicies.

- **API Gateway**: Accepts external traffic, talks to backend services.
- **Backend Services**: Accept traffic ONLY from API Gateway or other authorized services.
- **Database**: Accepts traffic ONLY from authorized backend services.

Apply policies:
```bash
kubectl apply -f k8s/network-policy-api-gateway.yaml
kubectl apply -f k8s/network-policy-backend.yaml
kubectl apply -f k8s/network-policy-database.yaml
```
