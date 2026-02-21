#!/bin/sh

set -euo pipefail

echo -e "\nConfiguring ArgoCD to manage the habitquest applications..."

# Wait for the admin secret to be available
echo -e "\nWaiting for ArgoCD initial admin secret..."
while ! kubectl -n argocd get secret argocd-initial-admin-secret &> /dev/null; do
  sleep 5
done

argocd_admin_password=$(kubectl -n argocd get secret argocd-initial-admin-secret \
 -o jsonpath="{.data.password}" | base64 -d; echo)

# retrieve argocd public ip
argocd_server_ip=$(kubectl -n argocd get service argocd-server -o jsonpath="{.status.loadBalancer.ingress[0].ip}")
argocd login "$argocd_server_ip" --username admin --password "$argocd_admin_password" --insecure

argocd app create tracking-service \
 --repo \
 https://github.com/myhabitquest/habitquest-deployment.git \
 --path kubernetes/applications/tracking-service/ \
  --dest-server https://kubernetes.default.svc \
 --dest-namespace default \
 --sync-policy auto \
 --auto-prune

argocd app create marketplace-service \
 --repo \
 https://github.com/myhabitquest/habitquest-deployment.git \
 --path kubernetes/applications/marketplace-service/ \
  --dest-server https://kubernetes.default.svc \
 --dest-namespace default \
 --sync-policy auto \
 --auto-prune

argocd app create guild-service \
 --repo \
 https://github.com/myhabitquest/habitquest-deployment.git \
 --path kubernetes/applications/guild-service/ \
  --dest-server https://kubernetes.default.svc \
 --dest-namespace default \
 --sync-policy auto \
 --auto-prune

argocd app create notification-service \
 --repo \
 https://github.com/myhabitquest/habitquest-deployment.git \
 --path kubernetes/applications/notification-service/ \
  --dest-server https://kubernetes.default.svc \
 --dest-namespace default \
 --sync-policy auto \
 --auto-prune

argocd app create edge-service \
 --repo \
 https://github.com/myhabitquest/habitquest-deployment.git \
 --path kubernetes/applications/edge-service/ \
  --dest-server https://kubernetes.default.svc \
 --dest-namespace default \
 --sync-policy auto \
 --auto-prune

argocd app create quest-service \
 --repo \
 https://github.com/myhabitquest/habitquest-deployment.git \
 --path kubernetes/applications/quest-service/ \
  --dest-server https://kubernetes.default.svc \
 --dest-namespace default \
 --sync-policy auto \
 --auto-prune