echo -e "\n Initializing Kubernetes cluster...\n"
minikube start --cpus 4 --memory 6g --driver docker --profile habitquest

echo -e "\n Enabling NGINX Ingress Controller...\n"
minikube addons enable ingress --profile habitquest

# minikube tunnel --profile habitquest &