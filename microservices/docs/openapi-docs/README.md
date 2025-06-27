# Apollo Microservices OpenAPI Documentation

Generated on: Fri Jun 27 13:00:20 EEST 2025

## Available Services

| Service | Status | JSON Spec | YAML Spec | Description |
|---------|--------|-----------|-----------|-------------|
| api-gateway | ✅ Available | [JSON](api-gateway-openapi.json) | ❌ | OpenAPI specification available |
| user-service | ✅ Available | [JSON](user-service-openapi.json) | ❌ | OpenAPI specification available |
| device-service | ✅ Available | [JSON](device-service-openapi.json) | ❌ | OpenAPI specification available |
| media-analysis-service | ✅ Available | [JSON](media-analysis-service-openapi.json) | ❌ | OpenAPI specification available |
| home-service | ✅ Available | [JSON](home-service-openapi.json) | ❌ | OpenAPI specification available |
| notification-service | ✅ Available | [JSON](notification-service-openapi.json) | ❌ | OpenAPI specification available |
| file-storage-service | ✅ Available | [JSON](file-storage-service-openapi.json) | ❌ | OpenAPI specification available |

## Usage

### Viewing Documentation

You can view the documentation in several ways:

1. **JSON Format**: Open any `*-openapi.json` file
2. **YAML Format**: Open any `*-openapi.yaml` file  
3. **Swagger UI**: Import any of these files into [Swagger Editor](https://editor.swagger.io/)
4. **VS Code**: Use the OpenAPI extension to preview the specs

### Updating Documentation

Run the update script to refresh all documentation:

```bash
./update-openapi-docs.sh
```

### Service URLs

| Service | URL | OpenAPI Endpoint |
|---------|-----|------------------|
| api-gateway | http://localhost:8080 | http://localhost:8080/v3/api-docs |
| user-service | http://localhost:8087 | http://localhost:8087/v3/api-docs |
| device-service | http://localhost:8082 | http://localhost:8082/v3/api-docs |
| media-analysis-service | http://localhost:8083 | http://localhost:8083/v3/api-docs |
| home-service | http://localhost:8084 | http://localhost:8084/v3/api-docs |
| notification-service | http://localhost:8085 | http://localhost:8085/v3/api-docs |
| file-storage-service | http://localhost:8086 | http://localhost:8086/v3/api-docs |

## Last Updated

Fri Jun 27 13:00:20 EEST 2025

---

*This documentation is automatically generated. Do not edit manually.*
