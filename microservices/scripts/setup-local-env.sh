#!/bin/bash

# Apollo Microservices - Local Environment Setup Script
# This script helps set up the local development environment

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MICROSERVICES_DIR="$(dirname "$SCRIPT_DIR")"

echo "🚀 Apollo Microservices - Local Environment Setup"
echo "=================================================="

# Check if .env file exists
if [ ! -f "$MICROSERVICES_DIR/.env" ]; then
    echo "📋 Creating .env file from template..."
    if [ -f "$MICROSERVICES_DIR/env.template" ]; then
        cp "$MICROSERVICES_DIR/env.template" "$MICROSERVICES_DIR/.env"
        echo "✅ Created .env file from template"
        echo "⚠️  IMPORTANT: Please edit .env file with your actual values before proceeding!"
        echo "   Required values to update:"
        echo "   - DB_USERNAME and DB_PASSWORD"
        echo "   - JWT_SECRET (base64 encoded)"
        echo "   - GOOGLE_IOS_CLIENT_ID"
        echo "   - GOOGLE_OAUTH_CLIENT_ID and GOOGLE_OAUTH_CLIENT_SECRET"
        echo "   - GEMINI_API_KEY"
        echo "   - MINIO_ACCESS_KEY and MINIO_SECRET_KEY"
        echo ""
        read -p "Press Enter after you've updated the .env file..."
    else
        echo "❌ env.template not found!"
        exit 1
    fi
else
    echo "✅ .env file already exists"
fi

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check for required tools
echo "🔍 Checking for required tools..."

if command_exists docker; then
    echo "✅ Docker is installed"
else
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

if command_exists docker compose; then
    echo "✅ Docker Compose is installed"
else
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Load environment variables
echo "📦 Loading environment variables..."
set -a
source "$MICROSERVICES_DIR/.env"
set +a
echo "✅ Environment variables loaded"

# Validate required environment variables
echo "🔐 Validating required environment variables..."
required_vars=(
    "DB_USERNAME"
    "DB_PASSWORD"
    "JWT_SECRET"
    "GEMINI_API_KEY"
    "GOOGLE_OAUTH_CLIENT_ID"
    "GOOGLE_OAUTH_CLIENT_SECRET"
    "MINIO_ACCESS_KEY"
    "MINIO_SECRET_KEY"
)

missing_vars=()
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ] || [ "${!var}" = "your_$(echo "$var" | tr '[:upper:]' '[:lower:]')" ] || [[ "${!var}" == *"your_"* ]]; then
        missing_vars+=("$var")
    fi
done

if [ ${#missing_vars[@]} -ne 0 ]; then
    echo "❌ The following required environment variables are missing or have placeholder values:"
    for var in "${missing_vars[@]}"; do
        echo "   - $var"
    done
    echo "Please update your .env file with actual values."
    exit 1
fi

echo "✅ All required environment variables are set"

# Offer to start services
echo ""
echo "🐳 Ready to start services!"
echo "Available options:"
echo "1. Start all services with Docker Compose"
echo "2. Start only infrastructure (PostgreSQL, MinIO, MQTT)"
echo "3. Exit (you can start services manually later)"
echo ""

read -p "Choose an option (1-3): " choice

case $choice in
    1)
        echo "🚀 Starting all services..."
        cd "$MICROSERVICES_DIR"
        docker-compose up -d
        echo "✅ All services started!"
        echo "🌐 Services are available at:"
        echo "   - API Gateway: http://localhost:${API_GATEWAY_PORT:-8080}"
        echo "   - MinIO Console: http://localhost:9001"
        echo "   - PostgreSQL: localhost:${DB_PORT:-5432}"
        ;;
    2)
        echo "🚀 Starting infrastructure services..."
        cd "$MICROSERVICES_DIR"
        docker-compose up -d postgres minio mosquitto
        echo "✅ Infrastructure services started!"
        echo "🌐 Infrastructure services are available at:"
        echo "   - PostgreSQL: localhost:${DB_PORT:-5432}"
        echo "   - MinIO: http://localhost:9000"
        echo "   - MinIO Console: http://localhost:9001"
        echo "   - MQTT Broker: localhost:1883"
        echo ""
        echo "💡 You can now start individual microservices from your IDE"
        ;;
    3)
        echo "👋 Setup complete! You can start services manually when ready."
        echo "💡 To start all services: docker-compose up -d"
        echo "💡 To start only infrastructure: docker-compose up -d postgres minio mosquitto"
        ;;
    *)
        echo "❌ Invalid option. Exiting."
        exit 1
        ;;
esac

echo ""
echo "🎉 Setup complete!"
echo "📚 For more information, see README-Configuration.md" 