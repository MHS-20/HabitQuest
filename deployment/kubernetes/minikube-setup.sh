echo -e "\n Initializing Kubernetes cluster...\n"
minikube start --cpus 4 --memory 6g --driver docker --profile habitquest

echo -e "\n Enabling NGINX Ingress Controller...\n"
minikube addons enable ingress --profile habitquest

echo -e "\n Starting minikube tunnel...\n"
minikube tunnel --profile habitquest > /tmp/minikube-tunnel.log 2>&1 &

TUNNEL_PID=$!
echo "Tunnel PID: $TUNNEL_PID (logs: /tmp/minikube-tunnel.log)"
echo $TUNNEL_PID > /tmp/minikube-tunnel.pid