# Runtime stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

ARG SERVICE_NAME
ARG SERVICE_PORT=8080

# Download OpenTelemetry Java Agent
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

# Copy pre-built JAR from local target directory
COPY ${SERVICE_NAME}/target/*.jar app.jar

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
