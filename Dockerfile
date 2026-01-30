# Generic JVM Dockerfile for Payment Gateway Services
FROM eclipse-temurin:21-jre-jammy

ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

WORKDIR /app

# Copy the built jar from the specific service directory
# The JAR is typically named [service-name]-1.0.0-SNAPSHOT.jar
COPY ${SERVICE_NAME}/target/${SERVICE_NAME}-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
