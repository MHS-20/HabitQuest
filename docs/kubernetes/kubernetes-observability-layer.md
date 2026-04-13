# Observability Layer - Kubernetes Deployment

The Observability Layer implements the complete stack for **monitoring**, **logging** and **distributed tracing** of the HabitQuest application.
It follows the principles of the **Three Pillars of Observability**: Metrics, Logs and Traces.

## Three Pillars of Observability
### 1. Metrics (Prometheus)

- Numerical time-series measurements (CPU, memory, request rate, latency)
- Prometheus scraping of `/actuator/prometheus`, metrics exposed via Spring Boot Actuator
- Namespace `prometheus-system`

Prometheus uses **Kubernetes Service Discovery** to automatically find the pods to scrape:
**Required pod annotations**:
```yaml
metadata:
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/port: "8080"
    prometheus.io/path: "/actuator/prometheus"
```

### 2. Logs (Loki + Fluent Bit)
- Log streams collected from application stdout
- Fluent Bit runs as a DaemonSet on every node, collects logs and forwards them to Loki.
- Loki is the log aggregator, allowing logs to be collected and queried
- Namespace `logging`

**Metadata added to each log**:

- `namespace`: Kubernetes namespace
- `pod`: Pod name
- `container`: Container name
- `labels`: All Kubernetes labels of the pod


### 3. Traces (Tempo)
- Traces the distributed flow of requests across the various microservices
- Spring Boot sends traces using the OpenTelemetry standard
- Namespace `prometheus-system`

Spring Boot services send traces to Tempo via configuration:
```yaml
env:
  - name: MANAGEMENT_OTLP_TRACING_ENDPOINT
    value: http://tempo.prometheus-system:4318/v1/traces
```

Spring Boot Micrometer produces traces automatically:

1. Instruments HTTP requests
2. Propagates trace context
3. Sends spans to Tempo via OTLP

### 4. Grafana
- Unified visualization of metrics, logs and traces
- Dashboards configured for the main aspects of the application
- Namespace `logging`

Available dashboards:

- `circuit-breaker.json`: Resilience4j circuit breakers statuses and metrics
- `http-rest-controllers.json`: REST endpoint performance and http traffic
- `jvm.json`: JVM heap, threads, GC metrics
- `logs.json`: Log aggregation of all services
- `spring-cloud-gateway.json`: Gateway routing metrics
- `traces.json`: Distributed tracing visualization

All components of the observability stack are deployed using parameterized Helm charts.