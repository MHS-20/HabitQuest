#!/bin/sh

set -euo pipefail

cd "$(dirname "$0")"

if ! kubectl -n grafana get configmap grafana-dashboards >/dev/null 2>&1; then
  kubectl -n grafana create configmap grafana-dashboards \
    --from-file=jvm.json=./resources/jvm.json \
    --from-file=circuit-breaker.json=./resources/circuit-breaker.json \
    --from-file=spring-cloud-gateway.json=./resources/spring-cloud-gateway.json
fi

# create namespace
if ! kubectl get ns grafana >/dev/null 2>&1; then
  kubectl create namespace grafana
fi

# add/update repo
helm repo add grafana "https://grafana.github.io/helm-charts" 2>/dev/null || true
helm repo update

# install/upgrade
helm upgrade --install habitquest-grafana grafana/grafana -n grafana -f resources/values.yml --wait --timeout 300s
echo "Grafana release 'habitquest-grafana' installed in namespace 'grafana'."