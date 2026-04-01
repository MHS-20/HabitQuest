#!/bin/bash
set -e
REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"

echo -e "\nBuilding service images inside Minikube...\n"
eval $(minikube -p habitquest docker-env)

SERVICES=(
  avatar-service
  edge-service
  guild-service
  marketplace-service
  notification-service
  quest-service
  tracking-service
)

for SERVICE in "${SERVICES[@]}"; do
    echo "Building $SERVICE..."
    ./gradlew ":services:$SERVICE:bootJar" -q
    docker build -t "$SERVICE:latest" "$REPO_ROOT/services/$SERVICE"
    echo "$SERVICE built"
done

echo -e "\nAll images built\n"