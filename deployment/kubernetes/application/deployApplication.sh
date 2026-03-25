#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TIMEOUT=120s

# Helper functions
deploy() {
  local SERVICE=$1
  echo -e "\n─── Deploying $SERVICE..."
  kubectl apply -k "$SCRIPT_DIR/$SERVICE"
}

wait_ready() {
  local SERVICE=$1
  echo "    Waiting for $SERVICE to be ready (timeout: $TIMEOUT)..."
  if kubectl wait \
      --for=condition=ready pod \
      --selector=app="$SERVICE" \
      --timeout="$TIMEOUT" 2>/dev/null; then
    echo "    ✓ $SERVICE is ready"
  else
    echo "    ✗ $SERVICE did not become ready in time"
    echo "    → Logs:"
    kubectl logs -l app="$SERVICE" --tail=20 2>/dev/null || true
    exit 1
  fi
}

# Core services (no outbound REST dependencies)
echo -e "\n Wave 1: core services "
deploy avatar-service
deploy tracking-service
wait_ready avatar-service
wait_ready tracking-service

# Services that call avatar
echo -e "\n Wave 2: services depending on avatar "
deploy guild-service
deploy marketplace-service
deploy quest-service
wait_ready guild-service
wait_ready marketplace-service
wait_ready quest-service

# Kafka consumer service
echo -e "\n Wave 3: kafka consumers "
deploy notification-service
wait_ready notification-service

# Gateway/edge service
echo -e "\n Wave 4: edge service "
deploy edge-service
wait_ready edge-service

# Summary
echo -e "\n"
echo   "  All services deployed successfully!"
echo   ""
echo ""
echo "  Pods:"
kubectl get pods -o wide
echo ""
echo "  Services:"
kubectl get services