# HabitQuest Deployment on Kubernetes

Il deployment dell'applicazione HabitQuest su Kubernetes è stato organizzato seguendo una chiara separazione delle responsabilità attraverso tre layer principali:
- **Application Layer**: servizi applicativi del dominio business
- **Platform Layer**: infrastruttura di base (message broker, ingress controller)
- **Observability Layer**: stack completo per monitoring, logging e tracing

Namespace Utilizzati:
```
default                  # Servizi applicativi
prometheus-system        # Prometheus e Tempo
logging                  # Loki, Fluent Bit, Grafana
```

## Deployment dei Layer
### 1. Platform Layer
Il platform layer deve essere deployato per primo in quanto fornisce l'infrastruttura di base:
- **Kafka**: Message broker con modalità KRaft (senza Zookeeper)
- **Kafka UI**: Interfaccia web per gestione e monitoring

### 2. Observability Layer
Lo stack di osservabilità viene deployato successivamente:
- **Prometheus**: Raccolta metriche
- **Tempo**: Distributed tracing
- **Loki**: Aggregazione log
- **Fluent Bit**: Shipping log dai pod
- **Grafana**: Visualizzazione unificata

### 3. Application Layer
Infine, i servizi applicativi vengono deployati con ordinamento basato sulle dipendenze:
**Ordine di deployment**:
1. `avatar-service`, `tracking-service` (servizi core senza dipendenze REST)
2. `guild-service`, `marketplace-service`, `quest-service` (dipendono da avatar-service)
3. `notification-service` (consumer Kafka)
4. `edge-service` (API Gateway)

## Configurazione di Rete
### Service Discovery

I servizi comunicano tramite DNS interno Kubernetes:
```
<service-name>.<namespace>:<port>
# Esempio:
avatar-service.default:8081
```

### Porte Esposte

| Servizio | Porta | Tipo |
|----------|-------|------|
| edge-service | 8080 | LoadBalancer |
| avatar-service | 8081 | ClusterIP |
| guild-service | 8082 | ClusterIP |
| marketplace-service | 8083 | ClusterIP |
| quest-service | 8084 | ClusterIP |
| tracking-service | 8085 | ClusterIP |
| notification-service | 8086 | ClusterIP |
| kafka-service | 9092, 8080 | LoadBalancer |

## Riferimenti

- [Documentazione Application Layer](./kubernetes-application-layer.md)
- [Documentazione Observability Layer](./kubernetes-observability-layer.md)
