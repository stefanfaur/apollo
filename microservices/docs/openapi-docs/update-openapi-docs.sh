#!/bin/bash

# Apollo Microservices OpenAPI Documentation Updater
# This script fetches OpenAPI specs from all running microservices and saves them locally

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DOCS_DIR="$(dirname "$0")"
TIMESTAMP=$(date '+%Y-%m-%d_%H-%M-%S')
BACKUP_DIR="${DOCS_DIR}/backups/${TIMESTAMP}"

# Service definitions: name:port:context_path
declare -a SERVICES=(
    "api-gateway:8080:"
    "user-service:8087:"
    "device-service:8082:"
    "media-analysis-service:8083:"
    "home-service:8084:"
    "notification-service:8085:"
    "file-storage-service:8086:"
)

# Function to print colored output
print_status() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to check if service is running
check_service() {
    local service_name=$1
    local port=$2
    local context_path=$3
    
    local url="http://localhost:${port}${context_path}/v3/api-docs"
    
    if curl -s --connect-timeout 5 --max-time 10 "$url" > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# Function to fetch OpenAPI spec
fetch_openapi_spec() {
    local service_name=$1
    local port=$2
    local context_path=$3
    local output_file=$4
    
    local url="http://localhost:${port}${context_path}/v3/api-docs"
    
    print_status $BLUE "  Fetching from: $url"
    
    if curl -s --connect-timeout 10 --max-time 30 "$url" -o "$output_file"; then
        # Validate JSON
        if jq empty "$output_file" 2>/dev/null; then
            local size=$(wc -c < "$output_file")
            print_status $GREEN "  ✓ Successfully fetched ${service_name} OpenAPI spec (${size} bytes)"
            
            # Also create a YAML version
            local yaml_file="${output_file%.json}.yaml"
            if command -v yq >/dev/null 2>&1; then
                yq eval -P "$output_file" > "$yaml_file"
                print_status $GREEN "  ✓ YAML version created: $yaml_file"
            fi
            
            return 0
        else
            print_status $RED "  ✗ Invalid JSON received from ${service_name}"
            rm -f "$output_file"
            return 1
        fi
    else
        print_status $RED "  ✗ Failed to fetch from ${service_name}"
        return 1
    fi
}

# Function to create backup
create_backup() {
    if [ -d "$DOCS_DIR" ] && [ "$(ls -A "$DOCS_DIR" 2>/dev/null | grep -v backups | grep -v update-openapi-docs.sh)" ]; then
        print_status $YELLOW "Creating backup of existing docs..."
        mkdir -p "$BACKUP_DIR"
        find "$DOCS_DIR" -maxdepth 1 -name "*.json" -o -name "*.yaml" | while read file; do
            if [ -f "$file" ]; then
                cp "$file" "$BACKUP_DIR/"
            fi
        done
        print_status $GREEN "✓ Backup created in: $BACKUP_DIR"
    fi
}

# Function to generate summary
generate_summary() {
    local summary_file="${DOCS_DIR}/README.md"
    
    cat > "$summary_file" << EOF
# Apollo Microservices OpenAPI Documentation

Generated on: $(date)

## Available Services

| Service | Status | JSON Spec | YAML Spec | Description |
|---------|--------|-----------|-----------|-------------|
EOF

    for service_def in "${SERVICES[@]}"; do
        IFS=':' read -r service_name port context_path <<< "$service_def"
        local json_file="${DOCS_DIR}/${service_name}-openapi.json"
        local yaml_file="${DOCS_DIR}/${service_name}-openapi.yaml"
        
        local status="❌ Not Available"
        local json_link="❌"
        local yaml_link="❌"
        local description="Service not running or unreachable"
        
        if [ -f "$json_file" ]; then
            status="✅ Available"
            json_link="[JSON](${service_name}-openapi.json)"
            description="OpenAPI specification available"
            
            if [ -f "$yaml_file" ]; then
                yaml_link="[YAML](${service_name}-openapi.yaml)"
            fi
            
            # Extract description from OpenAPI spec if available
            local spec_desc=$(jq -r '.info.description // "API documentation"' "$json_file" 2>/dev/null || echo "API documentation")
            if [ "$spec_desc" != "null" ] && [ "$spec_desc" != "API documentation" ]; then
                description="$spec_desc"
            fi
        fi
        
        echo "| $service_name | $status | $json_link | $yaml_link | $description |" >> "$summary_file"
    done
    
    cat >> "$summary_file" << EOF

## Usage

### Viewing Documentation

You can view the documentation in several ways:

1. **JSON Format**: Open any \`*-openapi.json\` file
2. **YAML Format**: Open any \`*-openapi.yaml\` file  
3. **Swagger UI**: Import any of these files into [Swagger Editor](https://editor.swagger.io/)
4. **VS Code**: Use the OpenAPI extension to preview the specs

### Updating Documentation

Run the update script to refresh all documentation:

\`\`\`bash
./update-openapi-docs.sh
\`\`\`

### Service URLs

| Service | URL | OpenAPI Endpoint |
|---------|-----|------------------|
EOF

    for service_def in "${SERVICES[@]}"; do
        IFS=':' read -r service_name port context_path <<< "$service_def"
        echo "| $service_name | http://localhost:$port | http://localhost:${port}${context_path}/v3/api-docs |" >> "$summary_file"
    done
    
    cat >> "$summary_file" << EOF

## Last Updated

$(date)

---

*This documentation is automatically generated. Do not edit manually.*
EOF

    print_status $GREEN "✓ Summary generated: $summary_file"
}

# Main execution
main() {
    print_status $BLUE "🚀 Apollo Microservices OpenAPI Documentation Updater"
    print_status $BLUE "=================================================="
    
    # Create backup
    create_backup
    
    # Initialize counters
    local success_count=0
    local total_count=${#SERVICES[@]}
    
    # Process each service
    for service_def in "${SERVICES[@]}"; do
        IFS=':' read -r service_name port context_path <<< "$service_def"
        
        print_status $YELLOW "📡 Processing: $service_name"
        
        # Check if service is running
        if check_service "$service_name" "$port" "$context_path"; then
            print_status $GREEN "  ✓ Service is running on port $port"
            
            # Fetch OpenAPI spec
            local output_file="${DOCS_DIR}/${service_name}-openapi.json"
            if fetch_openapi_spec "$service_name" "$port" "$context_path" "$output_file"; then
                ((success_count++))
            fi
        else
            print_status $RED "  ✗ Service is not running or not accessible on port $port"
        fi
        
        echo
    done
    
    # Generate summary
    generate_summary
    
    # Final status
    print_status $BLUE "=================================================="
    print_status $GREEN "✅ Documentation update completed!"
    print_status $BLUE "📊 Successfully updated: $success_count/$total_count services"
    
    if [ $success_count -lt $total_count ]; then
        local failed_count=$((total_count - success_count))
        print_status $YELLOW "⚠️  $failed_count service(s) were not accessible"
        print_status $YELLOW "   Make sure all services are running and try again"
    fi
    
    print_status $BLUE "📁 Documentation location: $DOCS_DIR"
    print_status $BLUE "📄 Summary: ${DOCS_DIR}/README.md"
    
    if [ -d "$BACKUP_DIR" ]; then
        print_status $BLUE "💾 Backup: $BACKUP_DIR"
    fi
    
    echo
    print_status $GREEN "🎉 Done! Check the README.md for a summary of all available APIs."
}

# Check for required tools
check_dependencies() {
    local missing_tools=()
    
    if ! command -v curl >/dev/null 2>&1; then
        missing_tools+=("curl")
    fi
    
    if ! command -v jq >/dev/null 2>&1; then
        missing_tools+=("jq")
    fi
    
    if [ ${#missing_tools[@]} -ne 0 ]; then
        print_status $RED "❌ Missing required tools: ${missing_tools[*]}"
        print_status $YELLOW "Please install them and try again:"
        
        for tool in "${missing_tools[@]}"; do
            case $tool in
                curl)
                    print_status $BLUE "  - curl: brew install curl (macOS) or apt-get install curl (Ubuntu)"
                    ;;
                jq)
                    print_status $BLUE "  - jq: brew install jq (macOS) or apt-get install jq (Ubuntu)"
                    ;;
            esac
        done
        
        exit 1
    fi
    
    if ! command -v yq >/dev/null 2>&1; then
        print_status $YELLOW "⚠️  yq not found - YAML files will not be generated"
        print_status $BLUE "   Install with: brew install yq (macOS) or snap install yq (Ubuntu)"
    fi
}

# Show help
show_help() {
    cat << EOF
Apollo Microservices OpenAPI Documentation Updater

USAGE:
    $0 [OPTIONS]

OPTIONS:
    -h, --help     Show this help message
    --dry-run      Check services without fetching documentation
    --backup-only  Only create backup, don't update docs

EXAMPLES:
    $0                 # Update all documentation
    $0 --dry-run       # Check which services are running
    $0 --backup-only   # Create backup only

SERVICES:
EOF

    for service_def in "${SERVICES[@]}"; do
        IFS=':' read -r service_name port context_path <<< "$service_def"
        echo "    - $service_name (port $port)"
    done
}

# Handle command line arguments
case "${1:-}" in
    -h|--help)
        show_help
        exit 0
        ;;
    --dry-run)
        print_status $BLUE "🔍 Dry run - checking service availability..."
        for service_def in "${SERVICES[@]}"; do
            IFS=':' read -r service_name port context_path <<< "$service_def"
            if check_service "$service_name" "$port" "$context_path"; then
                print_status $GREEN "✓ $service_name (port $port) - RUNNING"
            else
                print_status $RED "✗ $service_name (port $port) - NOT ACCESSIBLE"
            fi
        done
        exit 0
        ;;
    --backup-only)
        create_backup
        exit 0
        ;;
esac

# Run main program
check_dependencies
main 