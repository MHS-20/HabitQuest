#!/bin/sh

set -euo pipefail

cd "$(dirname "$0")"

echo "Installing Prometheus ..."

# create namespace if missing
if ! kubectl get ns "prometheus-system" >/dev/null 2>&1; then
  kubectl create namespace "prometheus-system"
fi

# add / update helm repo
helm repo add prometheus-community "https://prometheus-community.github.io/helm-charts" 2>/dev/null || true
helm repo update

# install or upgrade the chart
helm upgrade --install "habitquest-prometheus" "prometheus-community/prometheus" \
  --namespace "prometheus-system" \
  -f "resources/values.yml" \
  --wait --timeout "600s"

echo "Prometheus helm release 'habitquest-prometheus' installed in namespace 'prometheus-system'."
echo "Pods:"
kubectl get pods -n "prometheus-system" -o wide

echo "To access Prometheus:"
kubectl get svc -n "prometheus-system"
