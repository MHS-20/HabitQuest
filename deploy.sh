#!/bin/bash
set -e
find . -type f -name "*.sh" -exec chmod +x {} \;
./deployment/kubernetes/minikube-setup.sh
./deployment/kubernetes/build-images.sh
./deployment/kubernetes/platform/deployPlatform.sh
./deployment/kubernetes/application/deployApplication.sh
./deployment/kubernetes/observability/deployObservability.sh