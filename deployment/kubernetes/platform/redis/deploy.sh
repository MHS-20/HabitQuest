#!/bin/sh

set -euo pipefail

# Change to the script's directory
cd "$(dirname "$0")"

# Set the namespace for secrets
NAMESPACE="default"

echo -e "\n Deploying Redis..."

kubectl apply -f resources/redis.yml

sleep 5

echo -e "\n Waiting for Redis to be deployed..."

while [ $(kubectl get pod -l db=habitquest-redis | wc -l) -eq 0 ] ; do
  sleep 5
done

echo -e "\n Waiting for Redis to be ready..."

kubectl wait \
  --for=condition=ready pod \
  --selector=db=habitquest-redis \
  --timeout=180s

echo -e "\n Redis has been successfully deployed."
echo -e "\n Generating Secret with Redis credentials."

kubectl -n "$NAMESPACE" delete secret habitquest-redis-credentials --ignore-not-found=true
kubectl -n "$NAMESPACE" create secret generic habitquest-redis-credentials \
  --from-literal=spring.redis.host=habitquest-redis \
  --from-literal=spring.redis.port=6379
#  --from-literal=spring.redis.username=<redis_username> \
# --from-literal=spring.redis.password=<redis_password> \