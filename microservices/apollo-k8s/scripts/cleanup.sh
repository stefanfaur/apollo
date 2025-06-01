#!/bin/bash

# Apollo Microservices - Cleanup Script
# This script tears down the local k3d cluster and cleans up resources

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
CLUSTER_NAME="apollo"

echo -e "${YELLOW}🗑️  Apollo Microservices - Cleanup${NC}"
echo -e "${BLUE}===================================${NC}"
echo ""

# Confirm cleanup
confirm_cleanup() {
    echo -e "${YELLOW}⚠️  This will delete the entire Apollo k3d cluster and all data${NC}"
    echo -e "${RED}⚠️  This action cannot be undone!${NC}"
    echo ""
    read -p "Are you sure you want to continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}ℹ️  Cleanup cancelled${NC}"
        exit 0
    fi
}

# Check if cluster exists
check_cluster() {
    if ! k3d cluster list | grep -q "$CLUSTER_NAME"; then
        echo -e "${YELLOW}⚠️  Cluster '$CLUSTER_NAME' not found${NC}"
        echo -e "${BLUE}ℹ️  Nothing to clean up${NC}"
        exit 0
    fi
}

# Delete k3d cluster
delete_cluster() {
    echo -e "${YELLOW}🗑️  Deleting k3d cluster '$CLUSTER_NAME'...${NC}"
    
    k3d cluster delete "$CLUSTER_NAME"
    
    echo -e "${GREEN}✅ Cluster deleted successfully${NC}"
}

# Clean up kubectl contexts
cleanup_contexts() {
    echo -e "${YELLOW}🧹 Cleaning up kubectl contexts...${NC}"
    
    # Remove the k3d context if it exists
    CONTEXT_NAME="k3d-$CLUSTER_NAME"
    if kubectl config get-contexts -o name | grep -q "$CONTEXT_NAME"; then
        kubectl config delete-context "$CONTEXT_NAME" || true
        echo -e "${GREEN}✅ Context '$CONTEXT_NAME' removed${NC}"
    else
        echo -e "${BLUE}ℹ️  Context '$CONTEXT_NAME' not found${NC}"
    fi
}

# Clean up Docker resources (optional)
cleanup_docker() {
    echo -e "${YELLOW}🐳 Cleaning up Docker resources...${NC}"
    
    read -p "Do you want to remove Apollo Docker images? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}🗑️  Removing Apollo Docker images...${NC}"
        
        IMAGES=(
            "apollo/api-gateway"
            "apollo/user-service"
            "apollo/device-service"
            "apollo/media-analysis-service"
            "apollo/home-service"
            "apollo/notification-service"
            "apollo/file-storage-service"
        )
        
        for image in "${IMAGES[@]}"; do
            if docker images | grep -q "$image"; then
                docker rmi "$image:latest" || true
                echo -e "${GREEN}✅ Removed $image:latest${NC}"
            fi
        done
    else
        echo -e "${BLUE}ℹ️  Keeping Docker images${NC}"
    fi
    
    # Clean up unused Docker resources
    read -p "Do you want to clean up unused Docker resources? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}🧹 Cleaning up unused Docker resources...${NC}"
        docker system prune -f
        echo -e "${GREEN}✅ Docker cleanup completed${NC}"
    fi
}

# Display cleanup summary
display_summary() {
    echo -e "${GREEN}🎉 Cleanup completed successfully!${NC}"
    echo -e "${BLUE}================================${NC}"
    echo ""
    echo -e "${YELLOW}📋 What was cleaned up:${NC}"
    echo -e "  🗑️  k3d cluster '$CLUSTER_NAME'"
    echo -e "  🧹 kubectl context 'k3d-$CLUSTER_NAME'"
    echo -e "  📦 Associated Docker containers and networks"
    echo ""
    echo -e "${BLUE}ℹ️  To redeploy Apollo:${NC}"
    echo -e "  1. Build your Docker images"
    echo -e "  2. Run: ./scripts/deploy.sh"
    echo ""
}

main() {
    cd "$(dirname "$0")"
    
    confirm_cleanup
    check_cluster
    delete_cluster
    cleanup_contexts
    cleanup_docker
    display_summary
}

main "$@"