#!/bin/bash
echo "Deploying Kafka on Kubernetes..."
kubectl apply -f kafka-setup.yaml

echo -e "\nWaiting for Zookeeper to be ready..."
while [ $(kubectl get pod -l app=zookeeper 2>/dev/null | wc -l) -eq 0 ]; do
  sleep 5
done

echo "Waiting for Kafka pods to be ready..."
kubectl wait --for=condition=ready pod -l app=kafka --timeout=120s

while [ $(kubectl get pod -l app=kafka 2>/dev/null | wc -l) -eq 0 ]; do
  sleep 5
done

echo "Kafka deployment completed. Accessing Kafka UI at http://localhost:9093"

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    xdg-open http://localhost:9093 &> /dev/null
elif [[ "$OSTYPE" == "darwin"* ]]; then
    open http://localhost:9093
fi