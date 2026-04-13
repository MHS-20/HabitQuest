# Application Layer - Kubernetes Deployment

## Overview
The Application Layer contains all the microservices that implement the business logic of HabitQuest.
Each service is configured via Kustomize following the **Externalized Configuration** pattern.

## Externalized Configuration Pattern
Each service applies the **Externalized Configuration** pattern via Kustomize:

1. **Base Resources**: The base Kubernetes resources (Deployment, Service) are defined in each individual service project (`services/<service-name>/k8s`)
2. **Environment Overlays**: Environment-specific configurations are applied as **patches** via Kustomize
The base resources contain the fundamental configurations that do not vary across different environments, such as health checks,
while the patches introduce environment-specific environment variables, resource limits, and network configurations.

- Base resources remain in the service project
- Environment-specific configurations are centralized
- Easy management of multiple environments (dev, staging, prod)
- No YAML duplication

### File kustomization.yml
The `kustomization.yml` file is the central entry point that orchestrates:

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

# Reference to base resources in the service repository
resources:
- ../../../../services/quest-service/k8s

# Patches to apply to base resources
patches:
- path: patch-env.yml
- path: patch-resources.yml

# Container image management
images:
- name: quest-service
  newName: quest-service
  newTag: latest

# Replica configuration
replicas:
- count: 1
  name: quest-service
```

### 2. File patch-env.yml
**Purpose**: Externalizes all application configuration via environment variables.
**Example** (`quest-service/patch-env.yml`):

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

            # Service Endpoints
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
**Purpose**: Defines compute resource limits to ensure stability and prevent resource starvation.
**Example** (`quest-service/patch-resources.yml`):

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


