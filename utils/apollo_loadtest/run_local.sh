#!/bin/bash

# Apollo Load Test Runner
# Usage examples:
#   ./run_local.sh                 # local services on localhost
#   ./run_local.sh --remote        # target remote deployment (api.apollo.faur.sh)
#   USERS=100 ./run_local.sh --remote --headless  # remote, headless run

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_status() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Default values
USERS=${USERS:-50}
SPAWN_RATE=${SPAWN_RATE:-10}
RUN_TIME=${RUN_TIME:-5m}
REMOTE=false
HEADLESS=false

# --- Parse CLI flags ---
# Accept --remote and --headless in any order
for arg in "$@"; do
  case $arg in
    --remote)
      REMOTE=true
      shift
      ;;
    --headless)
      HEADLESS=true
      shift
      ;;
  esac
done

# Service URLs based on mode
if [ "$REMOTE" = true ]; then
    export API_GATEWAY_URL="https://api.apollo.faur.sh"
    export MQTT_HOST="faur.sh"
else
    export API_GATEWAY_URL="http://localhost:8080"
    export MQTT_HOST="localhost"
fi

print_status $BLUE "🚀 Apollo Load Test Runner"
print_status $BLUE "=========================="
print_status $YELLOW "Users: $USERS"
print_status $YELLOW "Spawn Rate: $SPAWN_RATE users/sec"
print_status $YELLOW "Duration: $RUN_TIME"
print_status $YELLOW "Mode: $( [ "$REMOTE" = true ] && echo "REMOTE" || echo "LOCAL" )"
print_status $YELLOW "API Gateway: $API_GATEWAY_URL"
print_status $YELLOW "MQTT Host: $MQTT_HOST"

# Check if uv is installed
if ! command -v uv &> /dev/null; then
    print_status $RED "❌ uv is not installed. Please install it first:"
    print_status $BLUE "   curl -LsSf https://astral.sh/uv/install.sh | sh"
    exit 1
fi

print_status $GREEN "✓ uv found"

# Install dependencies (cached by uv)
print_status $BLUE "📦 Installing dependencies..."
uv sync

print_status $GREEN "🏃 Starting Locust load test..."

LOCUST_CMD="uv run locust -f locustfile.py -u $USERS -r $SPAWN_RATE -t $RUN_TIME"

if [ "$HEADLESS" = true ]; then
    $LOCUST_CMD --headless --html=results.html --csv=results
else
    print_status $GREEN "🌐 Locust web UI will be available at: http://localhost:8089"
    print_status $BLUE "Press Ctrl+C to stop"
    $LOCUST_CMD --web-host=0.0.0.0 --web-port=8089
fi 