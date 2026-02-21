#!/bin/sh

set -euo pipefail

# Change to the script's directory
cd "$(dirname "$0")"

echo -e "\nInstalling ArgoCD..."

kubectl apply -k resources

echo -e "\nWaiting for ArgoCD to be deployed..."

sleep 15

while [ $(kubectl get pod -l app.kubernetes.io/name=argocd-server -n argocd 2>/dev/null | wc -l) -eq 0 ] ; do
  sleep 15
done

echo -e "\nWaiting for ArgoCD server to be ready..."

kubectl wait \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/name=argocd-server \
  --timeout=600s \
  --namespace=argocd

echo -e "\nArgoCD installation completed.\n"

