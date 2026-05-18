# Stage 1: Build and Compile the application
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

ARG SERVICE_NAME
#ENV SERVICE_NAME=${SERVICE_NAME}

WORKDIR /build

# Copy the built jar from the specific service directory
# The JAR is typically named [service-name]-1.0.0-SNAPSHOT.jar
COPY . .

RUN mvn clean package -pl ${SERVICE_NAME} -am -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-alpine

ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

WORKDIR /app

# Stage 3: Copy the built jar from the builder stage
COPY --from=builder /build/${SERVICE_NAME}/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
