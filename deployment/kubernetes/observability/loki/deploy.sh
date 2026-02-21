#!/bin/sh

set -euo pipefail

cd "$(dirname "$0")"

# ensure namespace exists
if ! kubectl get ns logging >/dev/null 2>&1; then
  kubectl create namespace logging
fi

# add helm repo
helm repo add grafana https://grafana.github.io/helm-charts 2>/dev/null || true
helm repo update
helm upgrade --install loki grafana/loki -n logging -f resources/values.yml --wait --timeout 300s
