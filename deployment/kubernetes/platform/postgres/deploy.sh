#!/bin/sh

set -euo pipefail

# Change to the script's directory
cd "$(dirname "$0")"

# Set the namespace for secrets
NAMESPACE="default"

echo -e "\n Deploying PostgreSQL..."

kubectl apply -f resources/postgresql.yml

sleep 5

echo -e "\n Waiting for PostgreSQL to be deployed..."

while [ $(kubectl get pod -l db=habitquest-postgres | wc -l) -eq 0 ] ; do
  sleep 5
done

echo -e "\n Waiting for PostgreSQL to be ready..."

kubectl wait \
  --for=condition=ready pod \
  --selector=db=habitquest-postgres \
  --timeout=180s

echo -e "\n PostgreSQL has been successfully deployed."
echo -e "\n Generating Secrets with PostgreSQL credentials."

kubectl -n "$NAMESPACE" delete secret habitquest-postgres-guild-credentials --ignore-not-found=true
kubectl -n "$NAMESPACE" create secret generic habitquest-postgres-guild-credentials \
  --from-literal=spring.datasource.url=jdbc:postgresql://habitquest-postgres:5432/habitquestdb_guild \
  --from-literal=spring.datasource.username=user \
  --from-literal=spring.datasource.password=password

kubectl -n "$NAMESPACE" delete secret habitquest-postgres-marketplace-credentials --ignore-not-found=true
kubectl -n "$NAMESPACE" create secret generic habitquest-postgres-marketplace-credentials \
  --from-literal=spring.datasource.url=jdbc:postgresql://habitquest-postgres:5432/habitquestdb_marketplace \
  --from-literal=spring.datasource.username=user \
  --from-literal=spring.datasource.password=password

kubectl -n "$NAMESPACE" delete secret habitquest-postgres-tracking-credentials --ignore-not-found=true
kubectl -n "$NAMESPACE" create secret generic habitquest-postgres-tracking-credentials \
  --from-literal=spring.datasource.url=jdbc:postgresql://habitquest-postgres:5432/habitquestdb_tracking \
  --from-literal=spring.datasource.username=user \
  --from-literal=spring.datasource.password=password


kubectl -n "$NAMESPACE" delete secret habitquest-postgres-quest-credentials --ignore-not-found=true
kubectl -n "$NAMESPACE" create secret generic habitquest-postgres-quest-credentials \
  --from-literal=spring.datasource.url=jdbc:postgresql://habitquest-postgres:5432/habitquestdb_quest \
  --from-literal=spring.datasource.username=user \
  --from-literal=spring.datasource.password=password