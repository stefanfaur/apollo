set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
CLUSTER_NAME="apollo"


echo -e "${GREEN}🚀 Apollo Microservices - Importing Images${NC}"
echo -e "${BLUE}================================================${NC}"


echo -e "${YELLOW}📦 Loading Docker images into k3d cluster...${NC}"
    
    IMAGES=(
        "apollo/api-gateway:latest"
        "apollo/user-service:latest"
        "apollo/device-service:latest"
        "apollo/media-analysis-service:latest"
        "apollo/home-service:latest"
        "apollo/notification-service:latest"
        "apollo/file-storage-service:latest"
    )
    
    for image in "${IMAGES[@]}"; do
        if docker image inspect $image &> /dev/null; then
            echo -e "${BLUE}📥 Loading $image...${NC}"
            k3d image import $image --cluster $CLUSTER_NAME
        else
            echo -e "${YELLOW}⚠️  Image $image not found locally. Skipping...${NC}"
        fi
    done
    
    echo -e "${GREEN}✅ Images loaded successfully${NC}"