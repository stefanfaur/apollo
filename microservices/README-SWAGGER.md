# Apollo Microservices - Centralized Swagger UI

## Overview

Apollo has a **centralized Swagger UI** accessible through the API Gateway. 
This provides a single entry point to explore and test all APIs across all microservices.

## Accessing Swagger UI

### Main Access Point

**Centralized Swagger UI:** `http://localhost:8080/swagger-ui.html`

This single URL provides access to all microservices' API documentation through a dropdown selector.

### Available Services

The centralized Swagger UI includes documentation for:

1. **API Gateway** - Gateway
2. **User Service** - Authentication and user management
3. **Device Service** - IoT device management
4. **Media Analysis Service** - AI  media analysis
5. **Home Service** - Home management
6. **Notification Service** - Notifications and content enrichment
7. **File Storage Service** - File upload and storage

## Usage Instructions

### 1. Start All Services

Ensure all microservices are running:

```bash
# Start infrastructure (database, MQTT, MinIO)
cd microservices/docker-infra
docker-compose up -d

# Start each microservice
cd microservices
mvn clean install
java -jar api-gateway/target/api-gateway-*.jar &
java -jar user-service/target/user-service-*.jar &
java -jar device-service/target/device-service-*.jar &
java -jar media-analysis-service/target/media-analysis-service-*.jar &
java -jar home-service/target/home-service-*.jar &
java -jar notification-service/target/notification-service-*.jar &
java -jar file-storage-service/target/file-storage-service-*.jar &
```

### 2. Access Swagger UI

1. Open your browser
2. Navigate to: `http://localhost:8080/swagger-ui.html`
3. Use the dropdown in the top-right to select different services
4. Explore/test APIs directly from the UI

### 3. Testing APIs

#### Authentication Required
Most endpoints require JWT authentication. To test protected endpoints:

1. First, use the **User Service** → **Auth Controller** → **POST /api/auth/login**
2. Copy the JWT token from the response
3. Click the **"Authorize"** button in Swagger UI
4. Enter: `Bearer YOUR_JWT_TOKEN`
5. Now you can test protected endpoints