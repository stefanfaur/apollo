#!/bin/bash

# Apollo Microservices - Docker Image Build Script
# This script builds Docker images for all microservices

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
REGISTRY=${REGISTRY:-"apollo"}
TAG=${TAG:-"latest"}

echo -e "${GREEN}🚀 Building Apollo Microservices Docker Images${NC}"
echo -e "${YELLOW}Registry: ${REGISTRY}${NC}"
echo -e "${YELLOW}Tag: ${TAG}${NC}"
echo ""

# Change to microservices directory
cd "$(dirname "$0")/.."

# Services to build
SERVICES=(
    "api-gateway"
    "user-service"
    "device-service"
    "media-analysis-service"
    "home-service"
    "notification-service"
    "file-storage-service"
)

# Build each service
for service in "${SERVICES[@]}"; do
    echo -e "${YELLOW}📦 Building ${service}...${NC}"
    
    if docker build -t "${REGISTRY}/${service}:${TAG}" -f "${service}/Dockerfile" .; then
        echo -e "${GREEN}✅ Successfully built ${REGISTRY}/${service}:${TAG}${NC}"
    else
        echo -e "${RED}❌ Failed to build ${service}${NC}"
        exit 1
    fi
    echo ""
done

echo -e "${GREEN}🎉 All images built successfully!${NC}"
echo ""
echo -e "${YELLOW}📋 Built images:${NC}"
for service in "${SERVICES[@]}"; do
    echo "  - ${REGISTRY}/${service}:${TAG}"
done

echo ""
echo -e "${YELLOW}💡 Next steps:${NC}"
echo "  - For local development: docker-compose up -d"
echo "  - For Kubernetes: ./scripts/deploy-k8s.sh" 