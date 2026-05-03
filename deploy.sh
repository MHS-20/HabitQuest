#!/bin/bash
set -e
find . -type f -name "*.sh" -exec chmod +x {} \;

./deployment/kubernetes/minikube-setup.sh
helm repo add stable https://charts.helm.sh/stable --force-update
./deployment/kubernetes/build-images.sh
./deployment/kubernetes/platform/deployPlatform.sh
./deployment/kubernetes/observability/deployObservability.sh
./deployment/kubernetes/application/deployApplication.sh

kubectl port-forward svc/edge-service 9000:9000 > /dev/null 2>&1 &
echo $! > f1.pid
kubectl -n logging port-forward svc/grafana 3000:80 > /dev/null 2>&1 &
echo $! > f2.pid
kubectl port-forward svc/kafka-service 8080:8080 > /dev/null 2>&1 &
echo $! > f3.pid

echo "Deployment completed."
echo ""
echo "\nGatewat listening at http://localhost:9000"
echo "\nAccess Grafana UI at http://localhost:3000 (credentials: admin/admin)"
echo "\nAccess Kafka UI at http://localhost:8080"