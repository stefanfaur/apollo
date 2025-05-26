# Multi-stage build for Notification Service
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# First, build shared-libs separately
COPY shared-libs/pom.xml ./shared-libs/
COPY shared-libs/src/ ./shared-libs/src/
WORKDIR /app/shared-libs
RUN mvn clean install -DskipTests

# Now build the Notification Service
WORKDIR /app/notification-service
COPY notification-service/pom.xml .
COPY notification-service/src/ ./src/

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine-3.21

# Add non-root user for security
RUN addgroup -g 1001 -S apollo && \
    adduser -u 1001 -S apollo -G apollo

# Set working directory
WORKDIR /app

# Copy the built JAR
COPY --from=builder /app/notification-service/target/*.jar app.jar

# Change ownership to apollo user
RUN chown apollo:apollo app.jar

# Switch to non-root user
USER apollo

# Expose port
EXPOSE 8085

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 