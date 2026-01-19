# Payment Gateway Helm Chart

This Helm chart deploys the Payment Gateway microservices with optimized configurations for GraalVM Native Image and CRaC-enabled services.

## Features

- **GraalVM Native Image Services**: api-gateway, auth-service, vault-service
  - Ultra-fast startup times (1s initialDelay probes)
  - Reduced memory footprint
  - Consistent performance without JIT warmup
  
- **CRaC-Enabled Payment Service**: 
  - Instant JVM warmup via checkpoint/restore
  - 1Gi PVC for checkpoint image storage
  - Extended startup probe (failureThreshold: 30) for restore operations
  
- **Security-Hardened Vault Service**:
  - Fully static binary with distroless container
  - Read-only root filesystem
  - No shell access
  
- **Spring Boot 4 Actuator Integration**:
  - All probes use `/actuator/health/readiness` and `/actuator/health/liveness`
  - Prometheus metrics enabled

## Prerequisites

- Kubernetes 1.25+
- Helm 3.8+
- PV provisioner support (for payment-service checkpoint storage)
- Nginx Ingress Controller (optional, for external access)

## Installation

### Quick Start

```bash
# Install with default values
helm install payment-gateway ./helm/payment-gateway

# Install with custom values
helm install payment-gateway ./helm/payment-gateway -f custom-values.yaml

# Install to specific namespace
helm install payment-gateway ./helm/payment-gateway --namespace payment-gateway --create-namespace
```

### Verify Installation

```bash
# Check pod status
kubectl get pods -l app.kubernetes.io/instance=payment-gateway

# Check services
kubectl get svc -l app.kubernetes.io/instance=payment-gateway

# Check PVC for payment-service
kubectl get pvc payment-service-checkpoint-pvc
```

## Configuration

### Key Configuration Options

#### GraalVM Native Services (api-gateway, auth-service, vault-service)

```yaml
apiGateway:
  probes:
    startup:
      initialDelaySeconds: 1  # Fast startup
      failureThreshold: 15    # 30s max startup time
```

#### CRaC-Enabled Payment Service

```yaml
paymentService:
  probes:
    startup:
      initialDelaySeconds: 5
      failureThreshold: 30    # 60s for checkpoint restore
  
  persistence:
    enabled: true
    size: 1Gi                 # Checkpoint storage
    mountPath: /crac-checkpoint
  
  sidecar:
    enabled: true             # CRaC checkpoint manager
```

#### Security-Hardened Vault Service

```yaml
vaultService:
  securityContext:
    runAsNonRoot: true
    runAsUser: 65532
    readOnlyRootFilesystem: true
    allowPrivilegeEscalation: false
```

### Resource Configuration

Default resource limits are conservative. Adjust based on your workload:

```yaml
# GraalVM Native Services (lower memory)
apiGateway:
  resources:
    requests:
      memory: "256Mi"
      cpu: "200m"
    limits:
      memory: "512Mi"
      cpu: "1000m"

# CRaC-Enabled Payment Service (higher memory for ZGC)
paymentService:
  resources:
    requests:
      memory: "2Gi"
      cpu: "500m"
    limits:
      memory: "4Gi"
      cpu: "2000m"
```

### Environment Variables

All services support custom environment variables:

```yaml
paymentService:
  env:
    - name: SPRING_PROFILES_ACTIVE
      value: "production"
    - name: JAVA_TOOL_OPTIONS
      value: "-XX:+UseZGC -XX:+ZGenerational"
```

## Probe Configuration Details

### GraalVM Native Image Services

- **Startup Probe**: `initialDelaySeconds: 1` (native images start in <100ms)
- **Readiness Probe**: `initialDelaySeconds: 1`
- **Liveness Probe**: `initialDelaySeconds: 10`

### CRaC-Enabled Payment Service

- **Startup Probe**: `initialDelaySeconds: 5`, `failureThreshold: 30` (allows 60s for restore)
- **Readiness Probe**: `initialDelaySeconds: 5`
- **Liveness Probe**: `initialDelaySeconds: 30`

All probes use Spring Boot 4 Actuator endpoints:
- Readiness: `/actuator/health/readiness`
- Liveness: `/actuator/health/liveness`

## Upgrading

```bash
# Upgrade to new version
helm upgrade payment-gateway ./helm/payment-gateway

# Upgrade with new values
helm upgrade payment-gateway ./helm/payment-gateway -f new-values.yaml

# Rollback to previous version
helm rollback payment-gateway
```

## Uninstallation

```bash
# Uninstall release
helm uninstall payment-gateway

# Delete PVC (if needed)
kubectl delete pvc payment-service-checkpoint-pvc
```

## Monitoring

All services expose Prometheus metrics at `/actuator/prometheus`:

```yaml
annotations:
  prometheus.io/scrape: "true"
  prometheus.io/port: "8080"
  prometheus.io/path: "/actuator/prometheus"
```

## Troubleshooting

### Payment Service Startup Issues

If payment-service fails to start within 60s:

```bash
# Check logs
kubectl logs -l app=payment-service

# Check CRaC sidecar logs
kubectl logs -l app=payment-service -c crac-manager

# Verify PVC is mounted
kubectl describe pod -l app=payment-service
```

### GraalVM Native Image Issues

If native services fail health checks:

```bash
# Check startup time
kubectl logs -l app=api-gateway --timestamps

# Verify actuator endpoints
kubectl exec -it <pod-name> -- wget -O- http://localhost:8080/actuator/health
```

## Performance Benchmarks

Expected startup times:
- **GraalVM Native Services**: 50-100ms
- **CRaC Payment Service**: 500ms-1s (with checkpoint)
- **Standard JVM Services**: 5-10s

Memory usage:
- **GraalVM Native Services**: 128-256Mi
- **CRaC Payment Service**: 2-4Gi (ZGC requires headroom)
- **Standard JVM Services**: 512Mi-1Gi
