#!/bin/bash

# Apollo Microservices - Secrets Creation Script
# This script creates Kubernetes secrets from .env file

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ENV_FILE="../.env"
NAMESPACE="apollo"
SECRET_NAME="apollo-secrets"

echo -e "${GREEN}🔐 Apollo Microservices - Secrets Creation${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# Check if .env file exists
check_env_file() {
    if [[ ! -f "$ENV_FILE" ]]; then
        echo -e "${RED}❌ .env file not found at $ENV_FILE${NC}"
        echo -e "${YELLOW}Please create a .env file based on env.template${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Found .env file${NC}"
}

# Load environment variables
load_env_vars() {
    echo -e "${YELLOW}📋 Loading environment variables...${NC}"
    
    # Source the .env file
    set -a  # automatically export all variables
    source "$ENV_FILE"
    set +a  # stop automatically exporting
    
    echo -e "${GREEN}✅ Environment variables loaded${NC}"
}

# Create namespace if it doesn't exist
create_namespace() {
    echo -e "${YELLOW}🏗️  Ensuring namespace exists...${NC}"
    
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        kubectl create namespace "$NAMESPACE"
        echo -e "${GREEN}✅ Namespace '$NAMESPACE' created${NC}"
    else
        echo -e "${BLUE}ℹ️  Namespace '$NAMESPACE' already exists${NC}"
    fi
}

# Create secrets
create_secrets() {
    echo -e "${YELLOW}🔑 Creating Kubernetes secrets...${NC}"
    
    # Delete existing secret if it exists
    if kubectl get secret "$SECRET_NAME" -n "$NAMESPACE" &> /dev/null; then
        echo -e "${YELLOW}⚠️  Secret '$SECRET_NAME' already exists, deleting...${NC}"
        kubectl delete secret "$SECRET_NAME" -n "$NAMESPACE"
    fi
    
    # Create the secret with all required values
    kubectl create secret generic "$SECRET_NAME" -n "$NAMESPACE" \
        --from-literal=jwt-secret="${JWT_SECRET:-apollo-jwt-secret-key}" \
        --from-literal=db-username="${DB_USERNAME:-apollo}" \
        --from-literal=db-password="${DB_PASSWORD:-apollo123}" \
        --from-literal=gemini-api-key="${GEMINI_API_KEY:-}" \
        --from-literal=google-ios-client-id="${GOOGLE_IOS_CLIENT_ID:-}" \
        --from-literal=google-oauth-client-id="${GOOGLE_OAUTH_CLIENT_ID:-}" \
        --from-literal=google-oauth-client-secret="${GOOGLE_OAUTH_CLIENT_SECRET:-}" \
        --from-literal=minio-access-key="${MINIO_ACCESS_KEY:-apollo}" \
        --from-literal=minio-secret-key="${MINIO_SECRET_KEY:-apollo123}" \
        --from-literal=grafana-admin-password="${GRAFANA_ADMIN_PASSWORD:-apollo123}"
    
    echo -e "${GREEN}✅ Secret '$SECRET_NAME' created successfully${NC}"
}

# Verify secrets
verify_secrets() {
    echo -e "${YELLOW}🔍 Verifying secrets...${NC}"
    
    if kubectl get secret "$SECRET_NAME" -n "$NAMESPACE" &> /dev/null; then
        echo -e "${GREEN}✅ Secret verification successful${NC}"
        echo -e "${BLUE}📋 Secret keys:${NC}"
        kubectl get secret "$SECRET_NAME" -n "$NAMESPACE" -o jsonpath='{.data}' | jq -r 'keys[]' | sed 's/^/  - /'
    else
        echo -e "${RED}❌ Secret verification failed${NC}"
        exit 1
    fi
}

# Display usage information
display_usage() {
    echo -e "${YELLOW}📋 Usage Information:${NC}"
    echo ""
    echo -e "${BLUE}The following secrets have been created:${NC}"
    echo -e "  🔑 jwt-secret: JWT signing key"
    echo -e "  🗄️  db-username: Database username"
    echo -e "  🗄️  db-password: Database password"
    echo -e "  🤖 gemini-api-key: Google Gemini API key"
    echo -e "  🔐 google-*: Google OAuth credentials"
    echo -e "  📦 minio-*: MinIO storage credentials"
    echo -e "  📊 grafana-admin-password: Grafana admin password"
    echo ""
    echo -e "${YELLOW}⚠️  Important Notes:${NC}"
    echo -e "  • Keep your .env file secure and never commit it to version control"
    echo -e "  • Update secrets by re-running this script after modifying .env"
    echo -e "  • Some services may need to be restarted after secret updates"
    echo ""
}

main() {
    cd "$(dirname "$0")"
    
    check_env_file
    load_env_vars
    create_namespace
    create_secrets
    verify_secrets
    display_usage
    
    echo -e "${GREEN}🎉 Secrets creation completed successfully!${NC}"
}

main "$@"