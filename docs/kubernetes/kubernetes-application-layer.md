# Application Layer - Deployment Kubernetes

## Panoramica
L'Application Layer contiene tutti i microservizi che implementano la logica di business di HabitQuest. 
Ogni servizio è configurato tramite Kustomize seguendo il pattern di **Externalized Configuration**.

## Architettura dei Servizi
```
application/
├── avatar-service/        # Gestione avatar e profili utente
├── edge-service/          # API Gateway (Spring Cloud Gateway)
├── guild-service/         # Gestione guild e team
├── marketplace-service/   # Marketplace oggetti e premi
├── notification-service/  # Invio notifiche email (Kafka consumer)
├── quest-service/         # Gestione quest e sfide
├── tracking-service/      # Tracciamento abitudini
└── deployApplication.sh   # Script orchestrazione deployment
```

## Pattern di Externalized Configuration
Ogni servizio applica il pattern **Externalized Configuration** tramite Kustomize:

1. **Base Resources**: Le risorse Kubernetes base (Deployment, Service) sono definite nel progetto del singolo servizio (`services/<nome-servizio>/k8s`)
2. **Environment Overlays**: Le configurazioni specifiche per ambiente vengono applicate come **patch** tramite Kustomize
Le risorse base contengono le configurazioni fondamentali che non variano tra ambienti diversi, come gli health checks,
mentre le patch introducono le variabili d'ambiente, i limiti di risorse e le configurazioni di rete specifiche per l'ambiente di deployment.

- Le risorse base rimangono nel progetto del servizio
- Le configurazioni specifiche dell'ambiente sono centralizzate
- Facile gestione di ambienti multipli (dev, staging, prod)
- Nessuna duplicazione di YAML

### File kustomization.yml
Il file `kustomization.yml` è il punto centrale che orchestra:

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

# Riferimento alle risorse base nel repository del servizio
resources:
- ../../../../services/quest-service/k8s

# Patch da applicare alle risorse base
patches:
- path: patch-env.yml
- path: patch-resources.yml

# Gestione immagini container
images:
- name: quest-service
  newName: quest-service
  newTag: latest

# Configurazione repliche
replicas:
- count: 1
  name: quest-service
```

### 2. File patch-env.yml
**Scopo**: Externalizza tutta la configurazione applicativa tramite variabili d'ambiente.
**Esempio** (`quest-service/patch-env.yml`):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: quest-service
spec:
  template:
    spec:
      containers:
        - name: quest-service
          env:
            # Spring Boot Profile
            - name: SPRING_PROFILES_ACTIVE
              value: prod

            # Service Discovery - altri microservizi
            - name: AVATAR_SERVICE_URI
              value: http://avatar-service:8081
            - name: TRACKING_SERVICE_URI
              value: http://tracking-service:8085

            # Message Broker
            - name: KAFKA_BROKER
              value: kafka-service:9092

            # Distributed Tracing
            - name: MANAGEMENT_OTLP_TRACING_ENDPOINT
              value: http://tempo.prometheus-system:4318/v1/traces
```

### 3. File patch-resources.yml
**Scopo**: Definisce i limiti di risorse compute per garantire stabilità e prevenire resource starvation.
**Esempio** (`quest-service/patch-resources.yml`):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: quest-service
spec:
  template:
    spec:
      containers:
        - name: quest-service
          resources:
            requests:
              memory: 756Mi    # Memoria garantita
              cpu: "0.1"       # CPU garantita (100m)
            limits:
              memory: 756Mi    # Limite massimo memoria
              cpu: "2"         # Limite massimo CPU
```


