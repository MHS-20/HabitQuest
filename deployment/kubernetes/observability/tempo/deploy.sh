cd "$(dirname "$0")"

kubectl create ns prometheus-system || true
helm repo add grafana https://grafana.github.io/helm-charts || true
helm repo update

helm upgrade --install tempo grafana/tempo \
  -n prometheus-system \
  -f resources/values.yml \
  --wait --timeout 200s

echo "http://tempo.prometheus-system.svc.cluster.local:3100"