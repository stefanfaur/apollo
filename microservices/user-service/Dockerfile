# Multi-stage build for User Service
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# First, build shared-libs separately
COPY shared-libs/pom.xml ./shared-libs/
COPY shared-libs/src/ ./shared-libs/src/
WORKDIR /app/shared-libs
RUN mvn clean install -DskipTests

# Now build the User Service
WORKDIR /app/user-service
COPY user-service/pom.xml .
COPY user-service/src/ ./src/

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine-3.21

# Add non-root user for security
RUN addgroup -g 1001 -S apollo && \
    adduser -u 1001 -S apollo -G apollo

# Set working directory
WORKDIR /app

# Download Pyroscope Java agent
RUN wget -O pyroscope.jar https://github.com/grafana/pyroscope-java/releases/download/v2.1.2/pyroscope.jar

# Copy the built JAR
COPY --from=builder /app/user-service/target/*.jar app.jar

# Change ownership to apollo user
RUN chown apollo:apollo app.jar pyroscope.jar

# Switch to non-root user
USER apollo

# Expose port
EXPOSE 8087

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8087/actuator/health || exit 1

# Run the application with Pyroscope agent
ENTRYPOINT ["java", "-javaagent:pyroscope.jar", "-jar", "app.jar"] 