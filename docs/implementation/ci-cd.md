# Report Tecnico — Pipeline CI/CD con GitHub Actions

## Introduzione

Il sistema di Continuous Integration e Continuous Delivery è interamente gestito tramite **GitHub Actions** e si articola in cinque workflow distinti. 
I workflow non sono isolati: comunicano tra loro tramite il meccanismo di **`repository_dispatch`**, formando una pipeline event-driven end-to-end che va dal commit del codice fino al deploy dell'infrastruttura su AWS EKS.

Il flusso complessivo segue questa catena:

```
[Push / PR]
    │
    ▼
┌───────────────────────────────┐
│ build.yml                     │
│-------------------------------│
│ Commit Lint                   │
│ K8s Validation                │
│ Detect Changes                │
│ Build & Test (servizi)        │
│ Package + Scan + Push GHCR    │
│ (main) dispatch semantic_rel  │
└───────────────────────────────┘
    │
    ▼
┌───────────────────────────────┐
│ semantic-release.yml          │
│-------------------------------│
│ Calcolo Tag SemVer            │
│ Retag immagine Docker GHCR    │
│ (main) dispatch update_manif  │
└───────────────────────────────┘
    │
    ▼
┌───────────────────────────────┐
│ update_manifest.yml           │
│-------------------------------│
│ Aggiorna kustomization.yaml   │
│ Apre PR su main               │
└───────────────────────────────┘
    │
    ▼
┌───────────────────────────────┐
│ provision.yml                 │
│-------------------------------│
│ Terraform su AWS              │
│ Deploy su EKS                 │
└───────────────────────────────┘
```


## Workflow 1 — `build.yml`: Build and Package

Questo è il workflow principale, il punto di ingresso dell'intera pipeline. Viene eseguito ad ogni `push` e ad ogni `pull_request` su qualsiasi branch.

### Validation
Prima di qualsiasi operazione sul codice, due job paralleli operano come **gate di qualità**:
- **`commit-lint`**: verifica che il messaggio di ogni commit rispetti la specifica **Conventional Commits**, prerequisito per il calcolo automatico del versioning semantico nel workflow successivo. Il job è saltato per i commit di Dependabot.
- **`k8s-validate`**: installa `kustomize` e `kubeconform` e valida i manifest Kubernetes presenti nel repository. `kustomize build k8s` genera il manifest finale (comprensivo degli overlay) e `kubeconform` verifica la correttezza strutturale in modalità strict contro lo schema ufficiale Kubernetes. Questo previene il deploy di manifest malformati.

### Rilevamento dei Servizi Modificati
Uno degli aspetti più rilevanti del workflow è l'**approccio incrementale**: non tutti i microservizi vengono ricompilati ad ogni push, ma solo quelli effettivamente modificati.
Il job `detect-changes` utilizza un filtro sul path per analizzare i file cambiati nel commit e produce la lista dei soli servizi toccati. Questo output viene passato ai job successivi.

### Build e Test
Il job `build` riceve la lista dei servizi modificati e avvia una **build matrix**: si esegue un job parallelo per ogni servizio modificato.
Per ogni servizio:
1. Viene configurato il JDK 21
2. Viene eseguita la build Gradle, che include compilazione, test ed altri controlli di qualità (lint, checkstyle, etc.)
3. Il JAR prodotto viene caricato come **artifact** di GitHub Actions, identificato univocamente per nome di servizio.
Il job `build-aggregate` funge da punto di sincronizzazione: raccoglie i risultati di tutti i job paralleli e fallisce l'intera pipeline se anche uno solo ha prodotto un errore, garantendo che il passaggio successivo avvenga solo con tutti i build verdi.

### Package, Scan e Publish
Questo job, anch'esso in matrix, si occupa della containerizzazione e della pubblicazione delle immagini. 
Esegue solo se `build-aggregate` ha avuto successo.

Per ogni servizio:
1. **Download dell'artifact**: recupera il JAR prodotto dal job precedente.
2. **Autenticazione su GHCR**: accede al GitHub Container Registry usando il token automatico `GITHUB_TOKEN`.
3. **Build dell'immagine Docker**: costruisce l'immagine usando il `Dockerfile` del singolo servizio, taggandola con lo SHA del commit (`github.sha`), che garantisce unicità e tracciabilità tra commit e immagine.
4. **Vulnerability Scan con Grype**: prima del push, l'immagine viene scansionata con **Grype** alla ricerca di vulnerabilità note. Il report viene prodotto in formato SARIF e caricato su GitHub Security, visibile nella tab *Security* del repository.
5. **Push dell'immagine**: l'immagine viene pubblicata su GHCR con due tag: lo **SHA del commit** (per la tracciabilità) e **`latest`** (per riferimento rapido).
6. **Trigger del workflow successivo**: se il push è avvenuto sul branch `main`, viene emesso un evento `repository_dispatch` di tipo `microservice_semantic_release`, passando come payload il nome del servizio e il path dell'immagine su GHCR.


## Workflow 2 — `build_ui.yml`: Build and Package Multiplatform UI
Questo workflow è dedicato esclusivamente al frontend **HabitQuest UI**, sviluppato con **Kotlin Multiplatform (KMP)** e Compose Multiplatform. 
È attivato solo su push o PR che toccano la directory `services/habitquest-ui/`.

Il workflow si articola in quattro job in sequenza:
1. **`commit-lint`**: stessa validazione Conventional Commits del workflow backend.
2. **`static-scan`**: scansione statica delle vulnerabilità nel codice sorgente (non nell'immagine) tramite `anchore/scan-action`, con upload del report SARIF su GitHub Security.
3. **`build`**: il job centrale esegue il build KMP con Gradle. Poiché i target KMP includono anche Kotlin/JS, vengono configurati sia il JDK 21 che Node.js 18.
4. **`lint-reports`**: scarica gli artifact del build e ri-carica separatamente i soli report di lint, rendendoli facilmente accessibili nella UI di GitHub Actions.


## Workflow 3 — `semantic-release.yml`: Semantic Release
Questo workflow è attivato esclusivamente da un evento `repository_dispatch` emesso da `build.yml` al termine di un push su `main`.

### Calcolo del Versioning Semantico
Il workflow utilizza **`semantic-release`** con il plugin **`semantic-release-monorepo`**, che adatta il comportamento standard di semantic-release ai repository monorepo.
Ogni servizio ha il proprio scope di versioning indipendente, con tag nel formato `<nome-servizio>-v<MAJOR>.<MINOR>.<PATCH>`.

Il versioning è calcolato automaticamente analizzando i messaggi di commit dall'ultimo tag:
- `feat:` → incremento **MINOR**
- `fix:` → incremento **PATCH**
- `!` → incremento **MAJOR**

### Retag dell'Immagine Docker
Dopodiché semantic-release si occupa di creare un nuovo tag su git e sul docker registry:
1. Viene effettuato il pull dell'immagine `latest` da GHCR.
2. L'immagine viene retaggata con la versione semantica calcolata (es. `2.3.1`).
3. La nuova versione viene pushata su GHCR, affiancando i tag SHA e `latest` già esistenti.
Se invece non ci sono cambiamenti rilevanti (es. solo commit `chore:` o `docs:`), nessun tag viene creato e il workflow si conclude senza emettere eventi successivi.

Al termine, se è stato calcolato una nuova version, viene emesso un secondo `repository_dispatch` di tipo `update_manifest` con payload contenente nome del servizio, path dell'immagine e versione semantica.
Ogni immagine è taggata con lo **SHA del commit** che l'ha generata, garantendo tracciabilità bidirezionale: da un'immagine in esecuzione nel cluster è sempre possibile risalire al commit esatto che l'ha prodotta.
Il tag semantico aggiuntivo fornisce invece leggibilità umana e permette di comunicare le versioni in modo significativo.

## Workflow 4 — `update_manifest.yml`: Update Manifest
Questo workflow, attivato da `repository_dispatch`, si rifà in parte al principio delle **GitOps**: 
l'unica fonte di verità per lo stato del cluster Kubernetes è il repository Git. 
Aggiornare i manifest in Git equivale a dichiarare l'intenzione di deploy.
Tuttavia il deploy completamente automatico è stato disabilitato (commentato nel file), 
per evitare di incorre in costi su AWS, ma l'infrastruttura è sarebbe pronta per rilasciare una nuova versione ad ogni push su `main`.

### Aggiornamento del Manifest Kustomize
Il job `update` modifica il tag dell'immagine nel file `kustomization.yaml` del servizio interessato.
Invece di committare direttamente su `main` (approccio commentato nel file), il workflow apre una **Pull Request** sul branch `chore/auto-updates`. 
Questo introduce un gate di revisione umana (o di approvazione ArgoCD) prima che la modifica raggiunga il branch principale e venga applicata al cluster.

## Workflow 5 — `provision.yml`: Terraform Provisioning
Questo workflow gestisce il provisioning dell'infrastruttura cloud e il deploy dei componenti di piattaforma su Kubernetes. 
Il trigger su `workflow_run` lo collega automaticamente al completamento di `Update Manifest`.
Il gruppo di concorrenza `deploy` con `cancel-in-progress: true` garantisce che non possano essere eseguiti due deploy contemporaneamente.

### Autenticazione con OIDC
L'autenticazione su AWS avviene tramite **OpenID Connect (OIDC)**, senza la necessità di memorizzare AWS Access Key come secret statici. 
GitHub Actions assume temporaneamente un ruolo IAM (`github-actions-terraform-role`), ottenendo credenziali a vita limitata.

### Provisioning con Terraform
La sequenza Terraform standard viene eseguita nella directory `./terraform`:
1. **`terraform init`**: inizializza il backend remoto (Terraform Cloud, configurato tramite `TF_API_TOKEN`).
2. **`terraform validate`**: verifica la sintassi e la coerenza dei file `.tf`.
3. **`terraform plan`**: calcola il diff tra lo stato attuale dell'infrastruttura e quello desiderato, producendo un piano.
4. **`terraform apply`**: applica il piano in modo non interattivo (`-auto-approve`).
L'ARN del ruolo IAM viene passato come variabile Terraform (`TF_VAR_terraform_role_arn`), mantenendo la configurazione esternalizzata.

### Deploy su EKS
Dopo il provisioning infrastrutturale, vengono installati `kubectl` e `helm` sul runner, 
e viene configurato l'accesso al cluster EKS tramite `aws eks update-kubeconfig`. Vengono quindi eseguiti due script shell:
- **`deployPlatform.sh`**: installa i componenti di piattaforma (probabilmente Ingress Controller, cert-manager, e simili).
- **`deployObservability.sh`**: installa lo stack di osservabilità (Prometheus, Grafana, Loki, Tempo).

## Integrazione Complessiva
| Workflow | Responsabilità | Trigger                                     |
|---|---|---------------------------------------------|
| `build.yml` | CI: build, test, scan, publish | Push / PR                                   |
| `build_ui.yml` | CI: frontend KMP | Push su `services/habitquest-ui/`           |
| `semantic-release.yml` | Versioning automatico | `repository_dispatch` da build              |
| `update_manifest.yml` | GitOps: aggiornamento manifest | `repository_dispatch` da semantic-release   |
| `provision.yml` | IaC: infrastruttura e deploy | Manuale / `workflow_run` di update-manifest |

