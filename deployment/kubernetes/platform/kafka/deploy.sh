#!/bin/bash
echo "Deploying Kafka on Kubernetes..."
cd "$(dirname "$0")"
kubectl apply -f kafka-setup.yml

#echo -e "\nWaiting for Zookeeper to be ready..."
#while [ $(kubectl get pod -l app=zookeeper 2>/dev/null | wc -l) -eq 0 ]; do
#  sleep 5
#done

echo "Waiting for Kafka pods to be ready..."
while [ $(kubectl get pod -l app=kafka 2>/dev/null | wc -l) -eq 0 ]; do
  sleep 5
done