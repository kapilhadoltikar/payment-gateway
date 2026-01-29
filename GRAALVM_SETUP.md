# GraalVM Installation Guide for Native Image Compilation

## Current Status

✅ **Standard JAR Build**: All services built successfully with OpenJDK Temurin 21 
❌ **Native Image Build**: Requires GraalVM 21+ installation

## Why GraalVM?

GraalVM is required for native image compilation because:
- It includes the `native-image` tool for AOT (Ahead-of-Time) compilation
- Produces standalone executables with ultra-fast startup (50-100ms)
- Reduces memory footprint by 50-75%
- Eliminates JIT warmup time

## Installation Options

### Option 1: SDKMAN! (Recommended for Linux/Mac/WSL)

```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install GraalVM 25
sdk install java 25-graal

# Verify installation
java -version
# Should show: "GraalVM CE 25..."

# Install native-image component
gu install native-image
```

### Option 2: Manual Installation (Windows)

1. **Download GraalVM**:
   - Visit: https://www.graalvm.org/downloads/
   - Download: GraalVM Community Edition 25 for Windows (x64)
   - File: `graalvm-community-jdk-25_windows-x64_bin.zip`

2. **Extract and Install**:
   ```powershell
   # Extract to C:\graalvm
   Expand-Archive -Path graalvm-community-jdk-25_windows-x64_bin.zip -DestinationPath C:\
   Rename-Item C:\graalvm-community-openjdk-25.0.1+11.1 C:\graalvm
   ```

3. **Set Environment Variables**:
   ```powershell
   # Set JAVA_HOME
   [System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\graalvm", "Machine")
   
   # Add to PATH
   $path = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
   $newPath = "C:\graalvm\bin;" + $path
   [System.Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")
   
   # Restart PowerShell
   ```

4. **Install Native Image**:
   ```powershell
   gu install native-image
   ```

5. **Install Build Tools** (Required for Windows):
   - install standalone Build Tools: https://aka.ms/vs/17/release/vs_BuildTools.exe

6. **Verify Installation**:
   ```powershell
   java -version
   # Should show: GraalVM CE 25...
   
   native-image --version
   # Should show: GraalVM Native Image 25...
   ```

### Option 3: Chocolatey (Windows Package Manager)

```powershell
# Install Chocolatey (if not installed)
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install GraalVM
choco install graalvm-community --version=25.0.1

# Refresh environment
refreshenv

# Install native-image
gu install native-image
```

## Building Native Images

Once GraalVM is installed, build native images using the `-Pnative` profile:

### Build All Services

```powershell
# Build all native images
mvn clean package -Pnative -DskipTests

# This will take 5-15 minutes per service
# Memory required: 8GB+ RAM
```

### Build Individual Services

```powershell
# API Gateway
cd api-gateway
mvn clean package -Pnative -DskipTests

# Auth Service
cd auth-service
mvn clean package -Pnative -DskipTests

# Vault Service (static binary)
cd vault-service
mvn clean package -Pnative -DskipTests
```

### Using Task (Recommended)

```bash
# Build all native images
task build:native

# Build individual service
task build-native-service SERVICE=api-gateway
```

## Expected Results

After successful native image build:

```
api-gateway/target/api-gateway.exe          (~80-120 MB)
auth-service/target/auth-service.exe        (~80-120 MB)
vault-service/target/vault-service          (~50-80 MB, static binary)
```

### Startup Time Comparison

| Service | JVM Cold Start | Native Image |
|---------|---------------|--------------|
| api-gateway | 8-12s | 50-100ms |
| auth-service | 8-12s | 50-100ms |
| vault-service | 8-12s | 30-50ms |

### Memory Usage Comparison

| Service | JVM | Native Image | Savings |
|---------|-----|--------------|---------|
| api-gateway | 512Mi | 256Mi | 50% |
| auth-service | 512Mi | 256Mi | 50% |
| vault-service | 512Mi | 128Mi | 75% |

## Troubleshooting

### Issue: "native-image: command not found"

**Solution**: Install native-image component:
```bash
gu install native-image
```

### Issue: "Error: Default native-compiler executable 'cl.exe' not found"

**Solution**: Install Visual Studio Build Tools (Windows only)

### Issue: Build fails with "OutOfMemoryError"

**Solution**: Increase build memory:
```powershell
$env:MAVEN_OPTS="-Xmx10g"
mvn clean package -Pnative -DskipTests
```

### Issue: "Unsupported features in image heap"

**Solution**: Check reflection configuration in `META-INF/native-image/reflect-config.json`

## Alternative: Use Standard JARs

If you don't need native images, the standard JARs work perfectly:

```powershell
# Build standard JARs (no GraalVM required)
mvn clean install -DskipTests

# Run services
java -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar
java -jar auth-service/target/auth-service-1.0.0-SNAPSHOT.jar
java -jar payment-service/target/payment-service-1.0.0-SNAPSHOT.jar
```

## Next Steps

1. ✅ Install GraalVM 21+
2. ✅ Install native-image component
3. ✅ Build native images with Maven or Task
4. ✅ Run `task build:native`
5. ✅ Deploy with Helm: `helm install payment-gateway ./helm/payment-gateway`

## Resources

- GraalVM Downloads: https://www.graalvm.org/downloads/
- Native Image Documentation: https://www.graalvm.org/latest/reference-manual/native-image/
- Spring Boot Native Image Guide: https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html
- GraalVM Native Build Tools: https://graalvm.github.io/native-build-tools/latest/index.html
