#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Helper functions
deploy() {
  local SERVICE=$1
  echo -e "\nDeploying $SERVICE..."
  kustomize build "$SCRIPT_DIR/$SERVICE" | kubectl apply -f -
}

wait_ready() {
  local SERVICE=$1
  echo -e "\nWaiting for $SERVICE to be ready..."
  while [ $(kubectl get pods -l app="$SERVICE" --no-headers 2>/dev/null | grep -c "Running") -eq 0 ]; do
    sleep 5
  done
  echo "$SERVICE is ready"
}

# Core services (no outbound REST dependencies)
deploy avatar-service
deploy tracking-service
wait_ready avatar-service
wait_ready tracking-service

# Services that call avatar
deploy guild-service
deploy marketplace-service
deploy quest-service
wait_ready guild-service
wait_ready marketplace-service
wait_ready quest-service

# Kafka consumer service
deploy notification-service
wait_ready notification-service

# Gateway/edge service
deploy edge-service
wait_ready edge-service

# Summary
echo -e "\n"
echo "All services deployed successfully!"
echo ""
echo ""
echo "Pods:"
kubectl get pods -o wide
echo ""
echo "Services:"
kubectl get services