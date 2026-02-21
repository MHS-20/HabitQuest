# ArgoCD Credentials
kubectl -n argocd get secret argocd-initial-admin-secret -o json | jq -r 'if .data.username then .data.username else "YWRtaW4=" end' | base64 --decode; echo
kubectl -n argocd get secret argocd-initial-admin-secret -o json | jq -r '.data.password' | base64 --decode; echo

argocd_server_ip=$(kubectl -n argocd get service argocd-server -o jsonpath="{.status.loadBalancer.ingress[0].ip}")
argocd_server_port=$(kubectl -n argocd get service argocd-server -o jsonpath="{.spec.ports[0].port}")
echo " - Argo CD: http://$argocd_server_ip:$argocd_server_port"

# Keycloak Credentials (username = user)
kubectl -n keycloak-system get secret habitquest-keycloak -o json | jq -r '.data["admin-user"]' | base64 --decode; echo
kubectl -n keycloak-system get secret habitquest-keycloak -o json | jq -r '.data["admin-password"]' | base64 --decode; echo

keycloak_ip=$(kubectl get service habitquest-keycloak -n keycloak-system --no-headers -o custom-columns=":status.loadBalancer.ingress[0].ip")
echo " - Keycloak: http://$keycloak_ip/realms/habitquest"
echo " - Keycloak: http://$keycloak_ip/admin"

# Postgres Credentials
kubectl -n default get secret habitquest-postgres-wallet-credentials -o json | jq -r '.data["spring.datasource.url"]' | base64 -d; echo
kubectl -n default get secret habitquest-postgres-wallet-credentials -o json | jq -r '.data["spring.datasource.username"]' | base64 -d; echo
kubectl -n default get secret habitquest-postgres-wallet-credentials -o json | jq -r '.data["spring.datasource.password"]' | base64 -d; echo

kubectl -n default get secret habitquest-postgres-sharing-credentials -o json | jq -r '.data["spring.datasource.url"]' | base64 -d; echo
kubectl -n default get secret habitquest-postgres-sharing-credentials -o json | jq -r '.data["spring.datasource.username"]' | base64 -d; echo
kubectl -n default get secret habitquest-postgres-sharing-credentials -o json | jq -r '.data["spring.datasource.password"]' | base64 -d; echo

kubectl -n default get secret habitquest-postgres-payment-credentials -o json | jq -r '.data["spring.datasource.url"]' | base64 -d; echo
kubectl -n default get secret habitquest-postgres-payment-credentials -o json | jq -r '.data["spring.datasource.username"]' | base64 -d; echo
kubectl -n default get secret habitquest-postgres-payment-credentials -o json | jq -r '.data["spring.datasource.password"]' | base64 -d; echo

# Redis Credentials
kubectl -n default get secret habitquest-redis-credentials -o json | jq -r '.data["spring.redis.host"]' | base64 --decode; echo
kubectl -n default get secret habitquest-redis-credentials -o json | jq -r '.data["spring.redis.port"]' | base64 --decode; echo

# Rabbitmq operator credentials
kubectl -n rabbitmq-system port-forward svc/habitquest-rabbitmq 15672:15672 # localhost:15672
kubectl -n rabbitmq-system get secret habitquest-rabbitmq-default-user -o json | jq -r '.data.username' | base64 --decode; echo
kubectl -n rabbitmq-system get secret habitquest-rabbitmq-default-user -o json | jq -r '.data.password' | base64 --decode; echo

# Rabbitmq application credentials
kubectl -n default get secret habitquest-rabbitmq-credentials -o json | jq -r '.data["spring.rabbitmq.username"]' | base64 --decode; echo
kubectl -n default get secret habitquest-rabbitmq-credentials -o json | jq -r '.data["spring.rabbitmq.password"]' | base64 --decode; echo
kubectl -n default get secret habitquest-rabbitmq-credentials -o json | jq -r '.data["spring.rabbitmq.host"]' | base64 --decode; echo
kubectl -n default get secret habitquest-rabbitmq-credentials -o json | jq -r '.data["spring.rabbitmq.port"]' | base64 --decode; echo