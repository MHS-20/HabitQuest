#!/bin/bash
set -e
find . -type f -name "*.sh" -exec chmod +x {} \;

./deployment/kubernetes/minikube-setup.sh
./deployment/kubernetes/build-images.sh
./deployment/kubernetes/platform/deployPlatform.sh
./deployment/kubernetes/application/deployApplication.sh
./deployment/kubernetes/observability/deployObservability.sh

kubectl port-forward svc/edge-service 9000:9000 > /dev/null 2>&1 &
echo $! > f1.pid
kubectl -n logging port-forward svc/grafana 3000:80 > /dev/null 2>&1 &
echo $! > f2.pid
echo "Deployment completed. Access Grafana at http://localhost:3000 (default credentials: admin/admin)."