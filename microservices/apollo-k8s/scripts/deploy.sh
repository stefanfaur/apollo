#!/bin/bash

# Apollo Microservices - Local Deployment Script
# This script deploys the entire Apollo stack to a local k3d cluster

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
CLUSTER_NAME="apollo"
REGISTRY_NAME="apollo-registry"
K3D_PORT_MAPPING="-p 80:80@loadbalancer -p 443:443@loadbalancer -p 5432:5432@loadbalancer -p 1883:1883@loadbalancer -p 1884:1884@loadbalancer -p 9000:9000@loadbalancer -p 9001:9001@loadbalancer -p 7000-7020:7000-7020@loadbalancer"

# Parse command line arguments
QUICK_MODE=false
for arg in "$@"; do
    if [[ "$arg" == "--quick" ]]; then
        QUICK_MODE=true
    fi
done

if [[ "$QUICK_MODE" == "true" ]]; then
    echo -e "${GREEN}🚀 Apollo Microservices - Local Deployment (Quick Mode)${NC}"
    echo -e "${YELLOW}⚡ Quick mode enabled - skipping rollout waits${NC}"
else
    echo -e "${GREEN}🚀 Apollo Microservices - Local Deployment${NC}"
fi
echo -e "${BLUE}================================================${NC}"
echo ""

# Check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}🔍 Checking prerequisites...${NC}"
    
    # Check if required tools are installed
    for tool in docker k3d kubectl helm; do
        if ! command -v $tool &> /dev/null; then
            echo -e "${RED}❌ $tool is not installed. Please install it first.${NC}"
            exit 1
        fi
    done
    
    # Check if Docker is running
    if ! docker info &> /dev/null; then
        echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ All prerequisites met${NC}"
}

# Create k3d cluster
create_cluster() {
    echo -e "${YELLOW}🏗️  Creating k3d cluster...${NC}"
    
    # Check if cluster already exists
    if k3d cluster list | grep -q $CLUSTER_NAME; then
        echo -e "${YELLOW}⚠️  Cluster '$CLUSTER_NAME' already exists${NC}"
        read -p "Do you want to delete and recreate it? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo -e "${YELLOW}🗑️  Deleting existing cluster...${NC}"
            k3d cluster delete $CLUSTER_NAME
        else
            echo -e "${BLUE}ℹ️  Using existing cluster${NC}"
            return
        fi
    fi
    
    # Create new cluster
    echo -e "${YELLOW}🔨 Creating new k3d cluster '$CLUSTER_NAME'...${NC}"
    k3d cluster create $CLUSTER_NAME \
        --agents 1 \
        $K3D_PORT_MAPPING \
        --wait
    
    # Set kubectl context
    kubectl config use-context k3d-$CLUSTER_NAME
    
    echo -e "${GREEN}✅ Cluster created successfully${NC}"
}

# Load Docker images
load_images() {
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
}

# Handle secrets creation
handle_secrets() {
    echo -e "${YELLOW}🔐 Managing secrets...${NC}"
    
    # Check if .env file exists
    if [[ ! -f "../.env" ]]; then
        echo -e "${RED}❌ .env file not found${NC}"
        echo -e "${YELLOW}Please create a .env file based on env.template${NC}"
        echo -e "${BLUE}Example: cp ../env.template ../.env${NC}"
        exit 1
    fi
    
    # Check if secrets already exist
    if kubectl get secret apollo-secrets -n apollo &> /dev/null; then
        echo -e "${GREEN}✅ Apollo secrets already exist${NC}"
        
        read -p "Do you want to recreate secrets from .env? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo -e "${BLUE}🔄 Recreating secrets...${NC}"
            ./create-secrets.sh
        else
            echo -e "${BLUE}ℹ️  Using existing secrets${NC}"
        fi
    else
        echo -e "${YELLOW}🔧 Creating secrets from .env file...${NC}"
        ./create-secrets.sh
        
        if [[ $? -eq 0 ]]; then
            echo -e "${GREEN}✅ Secrets created successfully${NC}"
        else
            echo -e "${RED}❌ Failed to create secrets${NC}"
            exit 1
        fi
    fi
}

# Deploy applications
deploy_applications() {
    echo -e "${YELLOW}🚀 Deploying Apollo applications...${NC}"
    
    # Build and apply local overlay
    echo -e "${YELLOW}📦 Building configuration with Kustomize...${NC}"
    if ! kubectl kustomize ../overlays/local --load-restrictor LoadRestrictionsNone | kubectl apply -f -; then
        echo -e "${RED}❌ Failed to apply Kustomize configuration${NC}"
        exit 1
    fi
    
    if [[ "$QUICK_MODE" == "false" ]]; then
        echo -e "${BLUE}⏳ Waiting for deployments to be ready...${NC}"
        
        # Wait for deployments
        DEPLOYMENTS=(
            "api-gateway"
            "user-service"
            "device-service"
            "media-analysis-service"
            "home-service"
            "postgres"
        )
        
        for deployment in "${DEPLOYMENTS[@]}"; do
            echo -e "${BLUE}⏳ Waiting for $deployment...${NC}"
            kubectl rollout status deployment/$deployment -n apollo --timeout=300s || true
        done
    else
        echo -e "${YELLOW}⚡ Skipping deployment rollout waits (quick mode)${NC}"
    fi
    
    echo -e "${GREEN}✅ Applications deployed${NC}"
}

# Deploy monitoring stack
deploy_monitoring() {
    echo -e "${YELLOW}📊 Deploying monitoring stack...${NC}"
    
    # Add Helm repositories
    echo -e "${BLUE}📚 Adding Helm repositories...${NC}"
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
    helm repo add grafana https://grafana.github.io/helm-charts
    helm repo add jetstack https://charts.jetstack.io
    helm repo update
    
    # Install Prometheus Operator CRDs first (required for ServiceMonitor)
    echo -e "${BLUE}📋 Installing Prometheus Operator CRDs...${NC}"
    kubectl apply --server-side -f https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/example/prometheus-operator-crd/monitoring.coreos.com_alertmanagerconfigs.yaml || true
    kubectl apply --server-side -f https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/example/prometheus-operator-crd/monitoring.coreos.com_alertmanagers.yaml || true
    kubectl apply --server-side -f https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/example/prometheus-operator-crd/monitoring.coreos.com_podmonitors.yaml || true
    kubectl apply --server-side -f https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/example/prometheus-operator-crd/monitoring.coreos.com_probes.yaml || true
    kubectl apply --server-side -f https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/example/prometheus-operator-crd/monitoring.coreos.com_prometheusagents.yaml || true
    kubectl apply --server-side -f https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/example/prometheus-operator-crd/monitoring.coreos.com_prometheuses.yaml || true
    kubectl apply --server-side -f https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/example/prometheus-operator-crd/monitoring.coreos.com_prometheusrules.yaml || true
    kubectl apply --server-side -f https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/example/prometheus-operator-crd/monitoring.coreos.com_scrapeconfigs.yaml || true
    kubectl apply --server-side -f https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/example/prometheus-operator-crd/monitoring.coreos.com_servicemonitors.yaml || true
    kubectl apply --server-side -f https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/example/prometheus-operator-crd/monitoring.coreos.com_thanosrulers.yaml || true
    
    # Deploy MinIO for storage
    echo -e "${BLUE}💾 Deploying MinIO for storage...${NC}"
    kubectl apply -f ../infra/minio.yaml || true
    
    if [[ "$QUICK_MODE" == "false" ]]; then
        # Wait for MinIO to be ready before deploying other components
        echo -e "${BLUE}⏳ Waiting for MinIO to be ready...${NC}"
        kubectl rollout status deployment/minio -n apollo --timeout=300s || true
        
        # Wait for MinIO bucket setup job to complete
        echo -e "${BLUE}⏳ Waiting for MinIO bucket setup to complete...${NC}"
        kubectl wait --for=condition=complete job/minio-bucket-setup -n apollo --timeout=300s || true
    else
        echo -e "${YELLOW}⚡ Skipping MinIO rollout wait (quick mode)${NC}"
    fi
    
    # Install cert-manager (required by Tempo Operator)
    echo -e "${BLUE}🔐 Installing cert-manager...${NC}"
    if ! kubectl get namespace cert-manager &> /dev/null; then
        helm upgrade --install cert-manager jetstack/cert-manager \
            --namespace cert-manager \
            --create-namespace \
            --set installCRDs=true \
            $(if [[ "$QUICK_MODE" == "false" ]]; then echo "--wait"; fi)
    else
        echo -e "${GREEN}✅ cert-manager already installed${NC}"
    fi
    
    # Install Tempo Operator
    echo -e "${BLUE}🔍 Installing Tempo Operator...${NC}"
    TEMPO_OPERATOR_VERSION="v0.9.0"
    kubectl apply -f "https://github.com/grafana/tempo-operator/releases/download/${TEMPO_OPERATOR_VERSION}/tempo-operator.yaml" || true
    
    if [[ "$QUICK_MODE" == "false" ]]; then
        # Wait for Tempo Operator to be ready
        echo -e "${BLUE}⏳ Waiting for Tempo Operator to be ready...${NC}"
        kubectl rollout status deployment/tempo-operator-controller -n tempo-operator-system --timeout=300s || true
    fi
    
    # Deploy Prometheus
    echo -e "${BLUE}📈 Deploying Prometheus...${NC}"
    helm upgrade --install prometheus prometheus-community/prometheus \
        --namespace monitoring --create-namespace \
        -f ../monitoring/prometheus-values.yaml \
        $(if [[ "$QUICK_MODE" == "false" ]]; then echo "--wait"; fi)
    
    # Deploy Grafana  
    echo -e "${BLUE}📊 Deploying Grafana...${NC}"
    helm upgrade --install grafana grafana/grafana \
        --namespace monitoring \
        -f ../monitoring/grafana-values.yaml \
        $(if [[ "$QUICK_MODE" == "false" ]]; then echo "--wait"; fi)
    
    # Deploy Loki Stack
    echo -e "${BLUE}📝 Deploying Loki Stack...${NC}"
    helm upgrade --install loki grafana/loki-stack \
        --namespace monitoring \
        -f ../monitoring/loki-values.yaml \
        $(if [[ "$QUICK_MODE" == "false" ]]; then echo "--wait"; fi)
    
    # Deploy Tempo via Operator (TempoMonolithic)
    # Couldn't get it to join ring if installing via the Helm Chart
    echo -e "${BLUE}🔍 Deploying Tempo via Operator...${NC}"
    kubectl apply -f ../monitoring/tempo-operator.yaml || true
    
    if [[ "$QUICK_MODE" == "false" ]]; then
        # Wait for Tempo deployment to be ready
        echo -e "${BLUE}⏳ Waiting for Tempo to be ready...${NC}"
        kubectl rollout status deployment/tempo-tempo -n monitoring --timeout=300s || true
    fi
    
    # Deploy Pyroscope
    echo -e "${BLUE}🔥 Deploying Pyroscope...${NC}"
    helm upgrade --install pyroscope grafana/pyroscope \
        --namespace monitoring \
        -f ../monitoring/pyroscope-values.yaml \
        $(if [[ "$QUICK_MODE" == "false" ]]; then echo "--wait"; fi)
    
    echo -e "${GREEN}✅ Monitoring stack deployed${NC}"
}

# Display access information
display_access_info() {
    echo -e "${GREEN}🎉 Deployment completed successfully!${NC}"
    echo -e "${BLUE}================================================${NC}"
    echo ""
    echo -e "${YELLOW}📋 Access Information:${NC}"
    echo ""
    echo -e "${BLUE}Apollo Services:${NC}"
    echo -e "  🌐 Main Gateway: http://apollo.local"
    echo -e "  🔧 API Services: http://api.apollo.local"
    echo ""
    echo -e "${BLUE}Monitoring Stack:${NC}"
    echo -e "  📊 Grafana: http://grafana.local (admin/apollo123)"
    echo -e "  📈 Prometheus: http://prometheus.local"
    echo -e "  🔍 Tempo (Operator): http://tempo-tempo:3200 (internal)"
    echo -e "  🔥 Pyroscope: http://pyroscope.local"
    echo ""
    echo -e "${YELLOW}⚠️  DNS Setup Required:${NC}"
    echo -e "Add the following lines to your /etc/hosts file:"
    echo ""
    echo -e "${BLUE}127.0.0.1 apollo.local api.apollo.local grafana.local prometheus.local pyroscope.local${NC}"
    echo ""
    echo -e "${GREEN}✅ What was deployed:${NC}"
    echo -e "  🏗️  k3d cluster '$CLUSTER_NAME' created"
    echo -e "  📦 Docker images loaded into cluster"  
    echo -e "  🔐 Secrets created from .env file"
    echo -e "  🚀 All Apollo microservices deployed"
    echo -e "  🔐 cert-manager deployed (required for Tempo Operator)"
    echo -e "  🔍 Tempo Operator deployed with TempoMonolithic CRD"
    echo -e "  📊 Complete monitoring stack deployed (Prometheus, Grafana, Loki, Tempo via Operator, Pyroscope)"
    echo ""
    echo -e "${YELLOW}🔧 Useful Commands:${NC}"
    echo -e "  📋 Check pods: kubectl get pods -A"
    echo -e "  📝 View logs: kubectl logs -f deployment/<service-name> -n apollo"
    echo -e "  🔍 Check Tempo: kubectl get tempomono tempo -n monitoring"
    echo -e "  🗑️  Cleanup: ./scripts/cleanup.sh"
    echo ""
    if [[ "$QUICK_MODE" == "true" ]]; then
        echo -e "${YELLOW}⚡ Quick mode was used - some components may still be starting up${NC}"
        echo -e "  📋 Check status: kubectl get pods -A"
        echo ""
    fi
}

main() {
    cd "$(dirname "$0")"
    
    check_prerequisites
    create_cluster
    load_images
    handle_secrets
    deploy_applications
    deploy_monitoring
    display_access_info
}

main "$@"