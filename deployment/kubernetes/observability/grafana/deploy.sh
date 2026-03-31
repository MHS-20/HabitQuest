#!/bin/sh
set -euo pipefail
cd "$(dirname "$0")"
echo "Installing Grafana ..."

if ! kubectl -n logging get configmap grafana-dashboards >/dev/null 2>&1; then
  kubectl -n logging create configmap grafana-dashboards \
    --from-file=jvm.json=./resources/jvm.json \
    --from-file=circuit-breaker.json=./resources/circuit-breaker.json \
    --from-file=spring-cloud-gateway.json=./resources/spring-cloud-gateway.json \
    --from-file=http-rest-controllers.json=./resources/http-rest-controllers.json \
    --from-file=kafka-dashboard.json=./resources/kafka-dashboard.json \
    --from-file=logs.json=./resources/logs.json \
    --from-file=traces.json=./resources/traces.json
fi

# create namespace
if ! kubectl get ns logging >/dev/null 2>&1; then
  kubectl create namespace logging
fi

# add/update repo
helm repo add grafana "https://grafana.github.io/helm-charts" 2>/dev/null || true
helm repo update

# install/upgrade
helm upgrade --install grafana grafana/grafana -n logging -f resources/values.yml
echo "Grafana release 'grafana' installed in namespace 'grafana'."