#!/bin/sh

set -euo pipefail

cd "$(dirname "$0")"

# ensure namespace exists
if ! kubectl get ns logging >/dev/null 2>&1; then
  kubectl create namespace logging
fi

# add helm repo
helm repo add fluent https://fluent.github.io/helm-charts 2>/dev/null || true
helm repo update

# install or upgrade fluent-bit using the provided values file
helm upgrade --install fluent-bit fluent/fluent-bit -f ./resources/values.yml --namespace logging --wait --timeout 200s
