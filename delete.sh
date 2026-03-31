#!/bin/bash
set -euo pipefail

PROFILE="habitquest"

# Stop tunnel
echo -e "\nStopping minikube tunnel..."
if [ -f /tmp/minikube-tunnel.pid ]; then
  TUNNEL_PID=$(cat /tmp/minikube-tunnel.pid)
  if kill -0 "$TUNNEL_PID" 2>/dev/null; then
    kill "$TUNNEL_PID"
    echo "  - Tunnel stopped (PID $TUNNEL_PID)"
  else
    echo "  - Tunnel process not found (already stopped?)"
  fi
  rm -f /tmp/minikube-tunnel.pid
  rm -f /tmp/minikube-tunnel.log
else
  echo "  - No tunnel PID file found, trying pkill..."
  pkill -f "minikube tunnel" || echo "  - No tunnel process found"
fi

# Delete cluster
echo -e "\nDeleting minikube cluster '$PROFILE'..."
minikube delete --profile "$PROFILE"

echo -e "\nCluster '$PROFILE' deleted successfully.\n"