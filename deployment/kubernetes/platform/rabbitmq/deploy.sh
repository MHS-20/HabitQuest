#!/bin/sh

set -euo pipefail

# Change to the script's directory
cd "$(dirname "$0")"

echo -e "\nRabbitMQ deployment started."

echo -e "\nInstalling RabbitMQ Cluster Kubernetes Operator..."

kubectl apply -f "https://github.com/rabbitmq/cluster-operator/releases/download/v2.9.0/cluster-operator.yml"

echo -e "\nWaiting for RabbitMQ Operator to be deployed..."

while [ $(kubectl get pod -l app.kubernetes.io/name=rabbitmq-cluster-operator -n rabbitmq-system | wc -l) -eq 0 ] ; do
  sleep 15
done

echo -e "\nWaiting for RabbitMQ Operator to be ready..."

kubectl wait \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/name=rabbitmq-cluster-operator \
  --timeout=300s \
  --namespace=rabbitmq-system

echo -e "\nThe RabbitMQ Cluster Kubernetes Operator has been successfully installed."

echo -e "\n-----------------------------------------------------"

echo -e "\nDeploying RabbitMQ cluster..."

kubectl apply -f resources/cluster.yml

echo -e "\nWaiting for RabbitMQ cluster to be deployed..."

while [ $(kubectl get pod -l app.kubernetes.io/name=habitquest-rabbitmq -n rabbitmq-system | wc -l) -eq 0 ] ; do
  sleep 15 
done

echo -e "\nWaiting for RabbitMQ cluster to be ready..."

kubectl wait \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/name=habitquest-rabbitmq \
  --timeout=600s \
  --namespace=rabbitmq-system

echo -e "\nThe RabbitMQ cluster has been successfully deployed."

echo -e "\n-----------------------------------------------------"

export RABBITMQ_USERNAME=$(kubectl get secret habitquest-rabbitmq-default-user -o jsonpath='{.data.username}' -n=rabbitmq-system | base64 --decode)
export RABBITMQ_PASSWORD=$(kubectl get secret habitquest-rabbitmq-default-user -o jsonpath='{.data.password}' -n=rabbitmq-system | base64 --decode)

echo "Username: $RABBITMQ_USERNAME"
echo "Password: $RABBITMQ_PASSWORD"

echo -e "\nGenerating Secret with RabbitMQ credentials."

kubectl delete secret habitquest-rabbitmq-credentials || true

kubectl create secret generic habitquest-rabbitmq-credentials \
    --from-literal=spring.rabbitmq.host=habitquest-rabbitmq.rabbitmq-system.svc.cluster.local \
    --from-literal=spring.rabbitmq.port=5672 \
    --from-literal=spring.rabbitmq.username="$RABBITMQ_USERNAME" \
    --from-literal=spring.rabbitmq.password="$RABBITMQ_PASSWORD"

unset RABBITMQ_USERNAME
unset RABBITMQ_PASSWORD

echo -e "\nSecret 'habitquest-rabbitmq-credentials' has been created for Spring Boot applications to interact with RabbitMQ."

echo -e "\nRabbitMQ deployment completed.\n"