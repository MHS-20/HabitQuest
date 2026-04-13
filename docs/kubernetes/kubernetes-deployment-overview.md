# HabitQuest Deployment on Kubernetes

The deployment of the HabitQuest application on Kubernetes has been organized following a clear separation of responsibilities through three main layers:

- **Application Layer**: application services for the business domain
- **Platform Layer**: base infrastructure (message broker, ingress controller)
- **Observability Layer**: complete stack for monitoring, logging and tracing

Namespaces Used:
```
default                  # Servizi applicativi
prometheus-system        # Prometheus e Tempo
logging                  # Loki, Fluent Bit, Grafana
```

## Layer Deployment
### 1. Platform Layer
The platform layer must be deployed first as it provides the base infrastructure:

- **Kafka**: Message broker with KRaft mode (without Zookeeper)
- **Kafka UI**: Web interface for management and monitoring

### 2. Observability Layer
The observability stack is deployed next:

- **Prometheus**: Metrics collection
- **Tempo**: Distributed tracing
- **Loki**: Log aggregation
- **Fluent Bit**: Log shipping from pods
- **Grafana**: Unified visualization

### 3. Application Layer
Finally, the application services are deployed with dependency-based ordering:
**Deployment order**:

1. `avatar-service`, `tracking-service` (core services with no REST dependencies)
2. `guild-service`, `marketplace-service`, `quest-service` (depend on avatar-service)
3. `notification-service` (Kafka consumer)
4. `edge-service` (API Gateway)

## Network Configuration
### Service Discovery

Services communicate via Kubernetes internal DNS:
```
<service-name>.<namespace>:<port>
# Example:
avatar-service.default:8081
```

### Exposed Ports

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


- [Application Layer Documentation](./kubernetes-application-layer.md)
- [Observability Layer Documentation](./kubernetes-observability-layer.md)
