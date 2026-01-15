# Stage 1: Build
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy POMs first to cache dependencies
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY merchant-service/pom.xml merchant-service/pom.xml
COPY vault-service/pom.xml vault-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

# Download dependencies (this layer will be cached unless POMs change)
# We use 'install' on the parent to ensure generic resolution, skipping tests
# We must also skip spring-boot repackage because we haven't compiled the code yet
RUN mvn clean install -DskipTests -Dmaven.main.skip -Dmaven.test.skip -Dspring-boot.repackage.skip=true

# Copy source code
COPY common/src common/src
COPY api-gateway/src api-gateway/src
COPY auth-service/src auth-service/src
COPY payment-service/src payment-service/src
COPY merchant-service/src merchant-service/src
COPY vault-service/src vault-service/src
COPY notification-service/src notification-service/src

# Build the final JARs
RUN mvn clean install -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

ARG SERVICE_NAME
ARG SERVICE_PORT=8080

# Download OpenTelemetry Java Agent
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

# Copy JAR from builder stage
COPY --from=builder /app/${SERVICE_NAME}/target/*.jar app.jar

# Environment variables for OpenTelemetry
ENV OTEL_SERVICE_NAME=${SERVICE_NAME}
ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_METRICS_EXPORTER=prometheus
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318/v1/traces
ENV OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf

EXPOSE ${SERVICE_PORT}

ENTRYPOINT ["java", \
    "--enable-preview", \
    "-javaagent:/app/opentelemetry-javaagent.jar", \
    "-jar", \
    "app.jar"]
