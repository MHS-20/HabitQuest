#!/bin/sh
set -euo pipefail
cd "$(dirname "$0")"
echo "Installing Tempo ..."

kubectl create ns prometheus-system || true
helm repo add grafana https://grafana.github.io/helm-charts || true
helm repo update

helm upgrade --install tempo grafana/tempo \
  -n prometheus-system \
  -f resources/values.yml

echo "http://tempo.prometheus-system.svc.cluster.local:3100"