# Get the directory where this script is located
# SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

"$SCRIPT_DIR/ingress-nginx/deploy.sh"
"$SCRIPT_DIR/redis/deploy.sh"
"$SCRIPT_DIR/postgres/deploy.sh"
"$SCRIPT_DIR/rabbitmq/deploy.sh"
"$SCRIPT_DIR/keycloak/deploy.sh"
"$SCRIPT_DIR/argocd/deploy.sh"

SERVICE_NAME="habitquest-keycloak"
NAMESPACE="keycloak-system"

echo -e "\nWaiting for Keycloak service to be ready..."
while ! kubectl get service "$SERVICE_NAME" -n "$NAMESPACE" &> /dev/null; do
  sleep 5
done

external_ip=$(kubectl get service "$SERVICE_NAME" -n "$NAMESPACE" --no-headers -o custom-columns=":status.loadBalancer.ingress[0].ip")
"$SCRIPT_DIR/keycloak/create-secrets.sh" "http://$external_ip/realms/habitquest"

echo -e "\nWaiting for ArgoCD server service to be ready..."
while ! kubectl get service argocd-server -n argocd &> /dev/null; do
  sleep 5
done

argocd_server_ip=$(kubectl -n argocd get service argocd-server -o jsonpath="{.status.loadBalancer.ingress[0].ip}")
argocd_server_port=$(kubectl -n argocd get service argocd-server -o jsonpath="{.spec.ports[0].port}")

"$SCRIPT_DIR/argocd/configure.sh"

echo -e "\n Kubernetes cluster has been successfully initialized."
echo -e "\n You can access the services using the following URLs:"
echo " - Argo CD: http://$argocd_server_ip:$argocd_server_port"
echo " - Keycloak: http://$external_ip/realms/habitquest"
echo " - Keycloak: http://$external_ip/admin"

