#!/bin/sh
set -euo pipefail
cd "$(dirname "$0")"

echo -e "\n Installing ingress-nginx..."
kubectl apply -k resources
echo -e "\n ingress-nginx installation completed.\n"