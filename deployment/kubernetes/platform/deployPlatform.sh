#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# "$SCRIPT_DIR/ingress-nginx/deploy.sh"
"$SCRIPT_DIR/kafka/deploy.sh"

echo -e "\n Kubernetes cluster has been successfully initialized."
echo -e " - Kafka UI: http://localhost:8080"

