#!/bin/bash
# set -euo pipefail

# File di output
OUTPUT_FILE="logs.txt"

# Pulisco il file precedente
> "$OUTPUT_FILE"

echo "Raccogliendo log dei pod in errore su Minikube..." | tee -a "$OUTPUT_FILE"

# Recupera tutti i pod che NON sono Running
FAILED_PODS=$(kubectl get pods --all-namespaces --field-selector=status.phase!=Running \
              -o jsonpath='{range .items[*]}{.metadata.namespace}/{.metadata.name}{"\n"}{end}')

if [[ -z "$FAILED_PODS" ]]; then
    echo "Nessun pod in stato di errore trovato." | tee -a "$OUTPUT_FILE"
    exit 0
fi

# Ciclo su ogni pod in errore
while read -r pod; do
    NS="${pod%%/*}"
    NAME="${pod##*/}"

    echo -e "\n===== Logs di $NAME (namespace: $NS) =====" | tee -a "$OUTPUT_FILE"

    # Recupera i log
    kubectl logs -n "$NS" "$NAME" --all-containers=true 2>&1 | tee -a "$OUTPUT_FILE"

done <<< "$FAILED_PODS"

echo -e "\nLog raccolti in $OUTPUT_FILE"