#!/bin/sh

set -euo pipefail

cd "$(dirname "$0")"

echo -e "\nUpdating Helm repos..."
helm repo update

echo -e "\nDeploying Prometheus..."
sh ./prometheus/deploy.sh

echo -e "\nDeploying Loki..."
sh ./loki/deploy.sh

echo -e "\nDeploying Fluent Bit..."
sh ./fluent-bit/deploy.sh

echo -e "\nDeploying Grafana..."
sh ./grafana/deploy.sh

echo -e "\nDeploying Tempo..."
sh ./tempo/deploy.sh

echo -e "\nWaiting for core pods to be Ready (prometheus, loki, grafana, fluent-bit, tempo)..."

# wait for pods
kubectl -n prometheus-system wait --for=condition=ready pod -l app.kubernetes.io/name=prometheus --timeout=300s || true
kubectl -n logging wait --for=condition=ready pod -l app.kubernetes.io/name=loki --timeout=300s || true
kubectl -n logging rollout status daemonset/fluent-bit --timeout=120s || true
kubectl -n grafana wait --for=condition=ready pod -l app.kubernetes.io/name=grafana --timeout=300s || true
kubectl -n prometheus-system wait --for=condition=ready pod -l app.kubernetes.io/name=tempo --timeout=300s || true

echo "Observability stack deployment finished. Check pod statuses:"
kubectl -n prometheus-system get pods -o wide || true
kubectl -n logging get pods -o wide || true
kubectl -n grafana get pods -o wide || true