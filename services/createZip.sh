#!/bin/bash

# ============================================================
# package_services.sh
# Crea uno zip con tutti i servizi presenti nella directory
# corrente, includendo solo il contenuto delle loro cartelle src/
# ============================================================

set -euo pipefail

# --- Configurazione ---
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
OUTPUT_ZIP="${SCRIPT_DIR}/services_${TIMESTAMP}.zip"
TEMP_DIR="$(mktemp -d)"

# Pulizia automatica del temp dir in caso di uscita anticipata
trap 'rm -rf "${TEMP_DIR}"' EXIT

echo "================================================"
echo "  Package Services Script"
echo "================================================"
echo "Directory sorgente : ${SCRIPT_DIR}"
echo "Output ZIP         : ${OUTPUT_ZIP}"
echo ""

# Verifica che zip sia installato
if ! command -v zip &>/dev/null; then
  echo "[ERRORE] Il comando 'zip' non è installato. Installalo e riprova."
  exit 1
fi

found=0

# Itera su tutte le sottocartelle dirette (i "servizi")
for service_dir in "${SCRIPT_DIR}"/*/; do
  [[ -d "${service_dir}" ]] || continue

  service_name="$(basename "${service_dir}")"
  src_path="${service_dir}src"

  # Salta i servizi senza cartella src/
  if [[ ! -d "${src_path}" ]]; then
    echo "[SKIP] ${service_name}  →  nessuna cartella src/ trovata"
    continue
  fi

  echo "[OK]   ${service_name}  →  aggiungo src/"

  # Copia nel temp dir mantenendo la struttura:
  # <nome-servizio>/src/<contenuto>
  mkdir -p "${TEMP_DIR}/${service_name}"
  cp -r "${src_path}" "${TEMP_DIR}/${service_name}/src"

  found=$((found + 1))
done

echo ""

if [[ ${found} -eq 0 ]]; then
  echo "[ERRORE] Nessun servizio con cartella src/ trovato. ZIP non creato."
  exit 1
fi

# Crea lo zip a partire dal temp dir
(
  cd "${TEMP_DIR}"
  zip -r "${OUTPUT_ZIP}" . -x "*.DS_Store" -x "__MACOSX/*"
)

echo "================================================"
echo "  Completato!"
echo "  Servizi inclusi : ${found}"
echo "  File creato     : ${OUTPUT_ZIP}"
echo "================================================"
