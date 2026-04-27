# Helm Chart Quick Reference

## Install Payment Gateway

```bash
# Install to default namespace
helm install payment-gateway ./helm/payment-gateway

# Install to custom namespace
helm install payment-gateway ./helm/payment-gateway \
  --namespace payment-gateway \
  --create-namespace
```

## Verify Deployment

```bash
# Check all pods
kubectl get pods

# Expected output:
# api-gateway-xxx          1/1     Running   (startup: 1s)
# auth-service-xxx         1/1     Running   (startup: 1s)
# vault-service-xxx        1/1     Running   (startup: 1s)
# payment-service-xxx      2/2     Running   (startup: 5-10s with CRaC)

# Check PVC for payment-service
kubectl get pvc payment-service-checkpoint-pvc
# Expected: Bound, 1Gi
```

## Access Services

```bash
# Port-forward to api-gateway
kubectl port-forward svc/api-gateway 8080:8080

# Test health endpoint
curl http://localhost:8080/actuator/health
```

## Monitor Startup Times

```bash
# Watch pod startup (GraalVM native should be <1s)
kubectl get pods -w

# Check detailed events
kubectl describe pod api-gateway-xxx
```

## Key Probe Configurations

- **GraalVM Services**: `initialDelaySeconds: 1`, `failureThreshold: 15`
- **Payment Service (CRaC)**: `initialDelaySeconds: 5`, `failureThreshold: 30`
- **All Services**: Spring Boot 4 Actuator endpoints (`/actuator/health/readiness`, `/actuator/health/liveness`)
