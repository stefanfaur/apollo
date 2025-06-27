# Apollo Load Testing

## Quick Start

### Prerequisites

- [uv](https://docs.astral.sh/uv/) installed (`curl -LsSf https://astral.sh/uv/install.sh | sh`)
- Apollo services running (locally or in remotely Kubernetes)

### Running Tests

```bash
# Interactive mode (local services)
./run_local.sh

# Interactive mode targeting remote deployment
./run_local.sh --remote

# Headless mode with custom parameters (remote)
USERS=100 SPAWN_RATE=20 RUN_TIME=10m ./run_local.sh --remote --headless


```

### Files

- `locustfile.py` - Main Locust test definitions with 3 user classes
- `pyproject.toml` - uv dependency configuration
- `run_local.sh` - Local test runner script

### User Classes

1. **MQTTDeviceUser** - Simulates hardware devices publishing MQTT messages
2. **ApiUser** - Simulates mobile app user flows through API Gateway (auth, homes, devices, unlock, fingerprint enrollment)

### Results

Results are saved as:
- `results.html` - HTML report (headless mode)
- `results_*.csv` - CSV statistics (headless mode)
- Live metrics in web UI (interactive mode)