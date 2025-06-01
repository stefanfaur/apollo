# Either source this script directly, or add the source line to your shell config (e.g., .bashrc, .zshrc).

# ─────────────────────────────────────────────────────────────
# Run kubectl in the “monitoring” namespace
function km() {
  # usage: km <kubectl-subcommand> [flags/args...]
  kubectl -n monitoring "$@"
}

# Run kubectl in the “apollo” namespace
function ka() {
  # usage: ka <kubectl-subcommand> [flags/args...]
  kubectl -n apollo "$@"
}

# ────────────────────────────────────────────────────────────────────────────
#   Aliases for the “monitoring” namespace:

# Get pods
alias kmp='km get pods'                        # e.g.  kmp
alias kmow='km get pods -o wide'                # prints wide output (node IP, etc.)

# Get deployments / services / statefulsets
alias kmd='km get deployments'                  # e.g.  kmd
alias kms='km get svc'                          # e.g.  kms
alias kmsa='km get statefulsets'                # e.g.  kmsa

# Describe resources
alias kmdp='km describe pod'                    # e.g.  kmdp grafana-64c5f7444d-q82nk
alias kmdd='km describe deployment'             # e.g.  kmdd prometheus-server-xyz

# Logs
# Usage: kmlf <pod-name> [ -c <container> ]
alias kml='km logs'                             # “km logs <pod>”
alias kmlf='km logs -f'                         # “km logs -f <pod>”

# Rollout status / restart
# Usage: kmrs deployment/<deployment-name>
alias kmrs='km rollout status'                   # e.g.  kmrs deployment/prometheus-server
alias kmrr='km rollout restart'                  # e.g.  kmrr deployment/loki

# Exec into a pod
# Usage: kmex <pod-name> [-- <command>]
alias kmex='km exec -it'                        # e.g.  kmex grafana-64c5f7444d-q82nk -- /bin/sh

# ────────────────────────────────────────────────────────────────────────────
#   Aliases for the “apollo” namespace:

# Get pods
alias kap='ka get pods'                         # e.g.  kap
alias kaow='ka get pods -o wide'                 # wide output

# Get deployments / services / statefulsets
alias kad='ka get deployments'                  # e.g.  kad
alias kas='ka get svc'                          # e.g.  kas
alias kasa='ka get statefulsets'                # e.g.  kasa

# Describe resources
alias kadp='ka describe pod'                    # e.g.  kadp device-service-6bd58bf984-4kjfv
alias kadd='ka describe deployment'             # e.g.  kadd user-service-558777746-4trkd

# Logs
# Usage: kalf <pod-name> [ -c <container> ]
alias kal='ka logs'                             # “ka logs <pod>”
alias kalf='ka logs -f'                         # “ka logs -f <pod>”

# Rollout status / restart
alias kars='ka rollout status'                   # e.g.  kars deployment/api-gateway
alias karr='ka rollout restart'                  # e.g.  karr deployment/home-service

# Exec into a pod
# Usage: kaex <pod-name> [-- <command>]
alias kaex='ka exec -it'                        # e.g.  kaex user-service-558777746-4trkd -- /bin/sh
