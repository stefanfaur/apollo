# Apollo Microservices Configuration Management

This document explains how to manage configuration and secrets for the Apollo microservices in a secure way that works across development, Docker, and Kubernetes environments.

## Quick Start

### 1. Local Development Setup

1. Copy the environment template:
   ```bash
   cp env.template .env
   ```

2. Edit `.env` with your actual values:
   ```bash
   # Database Configuration
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=apollo_db
   DB_USERNAME=your_actual_username
   DB_PASSWORD=your_actual_password
   
   # JWT Configuration
   JWT_SECRET=your_actual_jwt_secret_base64_encoded
   JWT_EXPIRATION=86400
   
   # Google OAuth Configuration
   GOOGLE_IOS_CLIENT_ID=your_actual_google_ios_client_id
   GOOGLE_OAUTH_CLIENT_ID=your_actual_google_oauth_client_id
   GOOGLE_OAUTH_CLIENT_SECRET=your_actual_google_oauth_client_secret
   
   # Gemini API Configuration
   GEMINI_API_KEY=your_actual_gemini_api_key
   
   # MinIO Configuration
   MINIO_ACCESS_KEY=your_actual_minio_access_key
   MINIO_SECRET_KEY=your_actual_minio_secret_key
   
   # ... other configurations
   ```

3. Load environment variables before running services:
   ```bash
   # Option 1: Source the .env file
   set -a && source .env && set +a
   
   # Option 2: Use your IntelliJ's environment variable configuration
   # Use `EnvFile` plugin for easy configuration(must add `.env` to each run config)
   
   # Option 3: Export variables manually
   export DB_USERNAME=your_username
   export DB_PASSWORD=your_password
   # ... etc
   ```

### 2. Docker Development Setup

1. Create your `.env` file as described above
2. Run with Docker Compose:
   ```bash
   docker-compose up -d
   ```

The Docker Compose file automatically reads from your `.env` file.

### 3. Kubernetes Production Setup

1. Create secrets:
   ```bash
   kubectl create secret generic apollo-secrets \
     --from-literal=db-username=your_username \
     --from-literal=db-password=your_password \
     --from-literal=jwt-secret=your_jwt_secret \
     --from-literal=google-ios-client-id=your_ios_client_id \
     --from-literal=google-oauth-client-id=your_oauth_client_id \
     --from-literal=google-oauth-client-secret=your_oauth_client_secret \
     --from-literal=gemini-api-key=your_api_key \
     --from-literal=minio-access-key=your_access_key \
     --from-literal=minio-secret-key=your_secret_key \
     --namespace=apollo
   ```

2. Apply configurations:
   ```bash
   kubectl apply -f k8s/namespace.yaml
   kubectl apply -f k8s/configmap.yaml
   kubectl apply -f k8s/user-service.yaml
   # ... apply other service configurations
   ```

## Configuration Files Structure

```
microservices/
├── .gitignore                    # Excludes .env and secret files
├── env.template                  # Template showing all required variables
├── docker-compose.yml            # Docker development environment
├── k8s/                         # Kubernetes configurations
│   ├── namespace.yaml           # Kubernetes namespace
│   ├── secrets.yaml             # Secrets template (DO NOT commit actual values)
│   ├── configmap.yaml           # Non-sensitive configuration
│   └── user-service.yaml        # Example service deployment
└── */src/main/resources/
    └── application.properties    # Now uses environment variables
```

## Environment Variables Reference

### Database Configuration
- `DB_HOST`: Database host (default: localhost)
- `DB_PORT`: Database port (default: 5432)
- `DB_NAME`: Database name (default: apollo_db)
- `DB_USERNAME`: Database username (required)
- `DB_PASSWORD`: Database password (required)

### Security Configuration
- `JWT_SECRET`: Base64 encoded JWT signing secret (required)
- `JWT_EXPIRATION`: JWT token expiration in seconds (default: 86400)

### Google OAuth Configuration
- `GOOGLE_IOS_CLIENT_ID`: Google OAuth iOS client ID (required for mobile app)
- `GOOGLE_OAUTH_CLIENT_ID`: Google OAuth web client ID (required for Spring Security OAuth)
- `GOOGLE_OAUTH_CLIENT_SECRET`: Google OAuth client secret (required for Spring Security OAuth)

### External Services
- `GEMINI_API_KEY`: Google Gemini API key (required for media-analysis-service)
- `OPENROUTER_API_KEY`: OpenRouter API key (optional)
- `MINIO_URL`: MinIO server URL (default: http://localhost:9000)
- `MINIO_ACCESS_KEY`: MinIO access key (required)
- `MINIO_SECRET_KEY`: MinIO secret key (required)
- `MINIO_BUCKET`: MinIO bucket name (default: apollo-bucket)
- `MQTT_BROKER_URL`: MQTT broker URL (default: tcp://localhost:1883)

### Service Configuration
- `API_GATEWAY_PORT`: API Gateway port (default: 8080)
- `USER_SERVICE_PORT`: User service port (default: 8087)
- `DEVICE_SERVICE_PORT`: Device service port (default: 8082)
- `MEDIA_ANALYSIS_SERVICE_PORT`: Media analysis service port (default: 8083)
- `HOME_SERVICE_PORT`: Home service port (default: 8084)
- `NOTIFICATION_SERVICE_PORT`: Notification service port (default: 8085)
- `FILE_STORAGE_SERVICE_PORT`: File storage service port (default: 8086)

### Inter-Service Communication
- `USER_SERVICE_URL`: User service URL for inter-service calls
- `DEVICE_SERVICE_URL`: Device service URL for inter-service calls
- `MEDIA_ANALYSIS_SERVICE_URL`: Media analysis service URL for inter-service calls
- `HOME_SERVICE_URL`: Home service URL for inter-service calls
- `NOTIFICATION_SERVICE_URL`: Notification service URL for inter-service calls
- `FILE_STORAGE_SERVICE_URL`: File storage service URL for inter-service calls

### Logging
- `LOG_LEVEL`: Logging level (default: INFO, options: DEBUG, INFO, WARN, ERROR)

## Security Best Practices

### 1. Never Commit Secrets
- The `.gitignore` file excludes all `.env` files and secret configurations
- Always use the template files and create your own local copies
- Use your CI/CD pipeline to inject secrets in production

### 2. Environment-Specific Configuration
- Use different `.env` files for different environments:
  - `.env.local` for local development
  - `.env.development` for development environment
  - `.env.staging` for staging environment
  - Production secrets should be managed by your deployment platform

### 3. Secret Rotation
- Regularly rotate sensitive values like JWT secrets, API keys, and database passwords
- Update secrets in all environments when rotating

### 4. Principle of Least Privilege
- Each service only has access to the secrets it needs
- Database users should have minimal required permissions

## Deployment Strategies

### Local Development
1. Use `.env` files with actual development values
2. Run services individually or use Docker Compose
3. Use development databases and external services

### Docker Development
1. Use Docker Compose with `.env` file
2. Services communicate via Docker network
3. Includes development versions of PostgreSQL, MinIO, and MQTT broker

### Kubernetes Production
1. Use Kubernetes Secrets for sensitive data
2. Use ConfigMaps for non-sensitive configuration
3. Implement proper resource limits and health checks
4. Use external managed services (RDS, S3, etc.) when possible

## Troubleshooting

### Missing Environment Variables
If you see errors about missing configuration, check:
1. Your `.env` file has all required variables
2. Variables are properly exported in your shell
3. Docker Compose is reading the `.env` file
4. Kubernetes secrets and configmaps are properly created

### Service Communication Issues
If services can't communicate:
1. Check service URLs are correct for your environment
2. Verify network connectivity (Docker networks, Kubernetes services)
3. Check firewall and security group settings

### Database Connection Issues
If database connections fail:
1. Verify database credentials
2. Check database host and port
3. Ensure database is running and accessible
4. Check network connectivity