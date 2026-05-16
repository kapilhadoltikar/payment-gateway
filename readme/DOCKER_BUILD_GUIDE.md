# Quick Start: Build Native Images with GraalVM Docker

## No GraalVM Installation Required!

Use Docker to build native images without installing GraalVM locally.

## Prerequisites

- ✅ Docker Desktop running
- ✅ Maven Wrapper installed (already done)
- ✅ Standard JARs built (already done)

## Build Native Images

### Option 1: Build All Services

```powershell
.\scripts\build-native-docker.ps1
```

### Option 2: Build Individual Services

```powershell
# API Gateway
.\scripts\build-native-docker.ps1 -Service api-gateway

# Auth Service
.\scripts\build-native-docker.ps1 -Service auth-service

# Vault Service (static binary)
.\scripts\build-native-docker.ps1 -Service vault-service
```

### Option 3: Manual Docker Build

```powershell
# API Gateway
docker build -f api-gateway/Dockerfile.native -t payment-gateway/api-gateway:1.0.0-native .

# Auth Service
docker build -f auth-service/Dockerfile.native -t payment-gateway/auth-service:1.0.0-native .

# Vault Service
docker build -f vault-service/Dockerfile.native -t payment-gateway/vault-service:1.0.0-static .
```

## Build Time

- **First build**: 10-15 minutes (downloads GraalVM image ~500MB)
- **Subsequent builds**: 5-10 minutes (cached layers)
- **Memory required**: 8GB+ RAM

## Test Native Images

```powershell
# Start infrastructure
docker-compose up -d

# Run api-gateway
docker run --rm -p 8080:8080 --network payment-gateway_default payment-gateway/api-gateway:1.0.0-native

# Run auth-service
docker run --rm -p 8081:8081 --network payment-gateway_default payment-gateway/auth-service:1.0.0-native

# Test
.\scripts\generate-test-jwt.ps1
.\scripts\verify-routing.ps1
```

## Expected Results

### Image Sizes
- **api-gateway**: ~80-120 MB
- **auth-service**: ~80-120 MB  
- **vault-service**: ~50-80 MB (static binary)

### Startup Time
- **Native images**: 50-100ms
- **vs JVM**: 8-12 seconds (100x faster!)

### Memory Usage
- **Native images**: 128-256 MB
- **vs JVM**: 512 MB-1 GB (50-75% reduction)

## Deploy to Kubernetes

```bash
# Update Helm values with native image tags
helm install payment-gateway ./helm/payment-gateway \
  --set apiGateway.image.repository=payment-gateway/api-gateway \
  --set apiGateway.image.tag=1.0.0-native \
  --set authService.image.repository=payment-gateway/auth-service \
  --set authService.image.tag=1.0.0-native
```

## Troubleshooting

**Issue**: Docker build fails with "Out of memory"
```powershell
# Increase Docker memory in Docker Desktop settings
# Settings > Resources > Memory > 8GB+
```

**Issue**: Build is slow
```powershell
# First build downloads ~500MB GraalVM image
# Subsequent builds use cached layers and are faster
```

## Next Steps

1. ✅ Build native images with Docker
2. ✅ Test locally
3. ✅ Push to container registry
4. ✅ Deploy with Helm
