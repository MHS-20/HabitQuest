#!/bin/bash
PROFILE="habitquest"

[ -s f1.pid ] && kill "$(cat f1.pid)"
[ -s f2.pid ] && kill "$(cat f2.pid)"
[ -s f3.pid ] && kill "$(cat f3.pid)"
rm -f f1.pid f2.pid f3.pid

echo -e "\nDeleting minikube cluster '$PROFILE'..."
minikube delete --profile "$PROFILE"
echo -e "\nCluster '$PROFILE' deleted successfully.\n"