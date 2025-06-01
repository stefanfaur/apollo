# Apollo Microservices - Kubernetes Deployment

This directory contains the complete Kubernetes deployment configuration for the Apollo platform.

## 🏗️ Architecture Overview

The deployment is organized into several key components:

- **Applications**: Individual microservice manifests
- **Infrastructure**: Shared infrastructure components (databases, ingress, etc.)
- **Monitoring**: Complete observability stack (Prometheus, Grafana, Loki, Tempo, Pyroscope)
- **Overlays**: Environment-specific configurations using Kustomize(local and prod)

## 📁 Directory Structure

```
apollo-k8s/
├── apps/                           # Microservice application manifests
│   ├── api-gateway/
│   ├── user-service/
│   ├── device-service/
│   ├── media-analysis-service/
│   ├── home-service/
│   ├── notification-service/
│   └── file-storage-service/
├── infra/                          # Infrastructure components
│   ├── namespace-apps.yaml         # Apollo application namespace
│   ├── namespace-obsv.yaml         # Monitoring namespace
│   ├── postgres.yaml               # PostgreSQL database
│   ├── ingress.yaml                # Traefik ingress rules
│   └── rbac.yaml                   # Service account and RBAC (needed for spring boot integration with K8s)
├── monitoring/                     # Observability stack configurations
│   ├── prometheus-values.yaml      # Prometheus Helm values
│   ├── grafana-values.yaml         # Grafana Helm values
│   ├── loki-values.yaml            # Loki Helm values
│   ├── tempo-operator.yaml         # Tempo Operator values
│   ├── pyroscope-values.yaml       # Pyroscope Helm values
├── overlays/                       # Environment-specific overlays
│   ├── local/                      # Local development configuration
│   └── production/                 # Production configuration
├── base/                           # Kustomize base configuration
└── scripts/                        # Deployment automation scripts
    ├── deploy.sh                   # Local deployment script
    ├── create-secrets.sh           # Secrets management script
    └── cleanup.sh                  # Environment cleanup script
```

## 🚀 Quick Start

### Prerequisites

Ensure you have the following tools installed:

- [Docker](https://docs.docker.com/get-docker/) (for building and running containers)
- [k3d](https://k3d.io/) (for local Kubernetes clusters)
- [kubectl](https://kubernetes.io/docs/tasks/tools/) (for Kubernetes management)
- [Helm](https://helm.sh/docs/intro/install/) (for monitoring stack deployment)

### 1. Environment Setup

1. **Copy the environment template:**
   ```bash
   cp ../env.template ../.env
   ```

2. **Edit the `.env` file with your actual values:**
   ```bash
   # Required: Set your actual API keys and secrets
   JWT_SECRET=your-super-secret-jwt-key-here
   GEMINI_API_KEY=your-gemini-api-key
   GOOGLE_OAUTH_CLIENT_ID=your-oauth-client-id
   GOOGLE_OAUTH_CLIENT_SECRET=your-oauth-client-secret
   # ... other values
   ```

**Note**: The deployment script will automatically create Kubernetes secrets from your `.env` file.

### 2. Build Docker Images

Build all Apollo microservice images:
```bash
cd ..  # Go back to microservices root
./scripts/build-images.sh
```

### 3. Deploy to Local Kubernetes

Deploy the entire stack with one command:
```bash
cd apollo-k8s
./scripts/deploy.sh
```

This script will:
- Create a k3d cluster named "apollo"
- Load Docker images into the cluster
- Automatically create secrets from your `.env` file
- Deploy all applications and infrastructure
- Set up the complete monitoring stack
- Provide access URLs and instructions

### 4. Access the Services

Add the following to your `/etc/hosts` file:
```
127.0.0.1 apollo.local api.apollo.local grafana.local prometheus.local tempo.local pyroscope.local
```

Then access:
- **Apollo Gateway**: http://apollo.local
- **Apollo APIs**: http://api.apollo.local
- **Grafana**: http://grafana.local (admin/apollo123)
- **Prometheus**: http://prometheus.local
- **Tempo**: http://tempo.local
- **Pyroscope**: http://pyroscope.local

## 🔧 Useful commands

### Shortcuts setup

As many commands are long are repetitive, and we only use 2 namespaces,
you can use the file `scripts/shortcuts.sh` to set up some shortcuts for kubectl commands.

It provides shortcuts for:

* **km** / **ka**: run `kubectl -n monitoring` / `kubectl -n apollo` followed by something else like `km get pods`

#### Specific shortcuts
Use `k<namespace-prefix><shortcut>`, where `namespace-prefix` is `m` for monitoring and `a` for apollo, and `shortcut` values are described below(`_` represents `namespace-prefix`):

* `k_p`: get pods
* `k_ow`: get pods –o wide
* `k_d`: get deployments
* `k_s`: get services
* `k_sa`: get statefulsets
* `k_dp`: describe pod
* `k_dd`: describe deployment
* `k_l`: logs
* `k_lf`: logs -f
* `k_rs`: rollout status
* `k_rr`: rollout restart
* `k_ex`: exec -it into a pod


### Viewing Logs

```bash
# View all pods
kubectl get pods -A

# View specific service logs
kubectl logs -f deployment/api-gateway -n apollo

# View monitoring stack logs
kubectl logs -f deployment/grafana -n monitoring
```

### Debugging

```bash
# Check service status
kubectl get deployments -n apollo

# Describe a problematic pod
kubectl describe pod <pod-name> -n apollo

# Port forward for direct access
kubectl port-forward service/api-gateway 8080:8080 -n apollo
```

## 📊 Observability Stack

The deployment includes an observability stack:

### Metrics (Prometheus + Grafana)
- **Prometheus** scrapes metrics from all Apollo services
- **Grafana** provides dashboards and visualization
- Pre-configured data sources

### Logs (Loki + Promtail)
- **Loki** aggregates logs from all pods
- **Promtail** collects logs automatically
- Searchable in Grafana's Drilldown section

### Traces (Tempo)
- **Tempo** stores distributed traces
- OpenTelemetry integration with all services
- Trace correlation in Grafana

### Profiling (Pyroscope)
- **Pyroscope** provides continuous profiling
- CPU and memory profiling for all services
- Integrated with Grafana for correlation

## 🌍 Production Deployment

### Manual Production Deployment


```bash
# Set your kubeconfig to point to production cluster
kubectl config use-context your-production-context

# Create secrets for production
./scripts/create-secrets.sh

# Deploy using production overlay
kubectl apply -k overlays/production/
```

### Production Considerations

- **Resource Limits**: Production overlay includes higher resource limits
- **Replicas**: Multiple replicas for high availability  
- **Persistence**: Persistent volumes for databases and monitoring
- **Security**: Consider network policies and pod security policies
- **TLS**: Configure TLS certificates for ingress
- **Monitoring**: Set up alerting rules in Prometheus
- **Registry Access**: Ensure proper image pull secrets for private registries
- **DNS Configuration**: Point domains to the cluster's external IP


## 🧹 Cleanup

To completely remove the local environment:
```bash
./scripts/cleanup.sh
```

This will:
- Delete the k3d cluster
- Remove kubectl contexts
- Optionally clean up Docker images and resources

## 🔧 Customization

### Adding New Services

1. **Create service directory**: `mkdir apps/new-service`
2. **Add deployment and service manifests**
3. **Update base kustomization**: Add to `base/kustomization.yaml`
4. **Update ingress**: Add routes in `infra/ingress.yaml`
5. **Update scripts**: Add to image lists in deployment scripts

### Modifying Monitoring

- **Prometheus**: Edit `monitoring/prometheus-values.yaml`
- **Grafana**: Edit `monitoring/grafana-values.yaml` or add dashboards
- **Loki**: Edit `monitoring/loki-values.yaml`
- **Custom dashboards**: Add JSON files to `monitoring/dashboards/`

### Environment-Specific Changes

- **Local**: Modify `overlays/local/patches/`
- **Production**: Modify `overlays/production/patches/`
- **New environment**: Create new overlay directory

## 🐛 Troubleshooting

### Common Issues

1. **Images not found**: Ensure Docker images are built and loaded
2. **Secrets missing**: Ensure `.env` file exists, then run `./scripts/create-secrets.sh` manually or redeploy
3. **DNS not working**: Check `/etc/hosts` entries
4. **Pods not starting**: Check resource limits and node capacity
---

**Note**: This deployment configuration is designed for development and mock production use. 