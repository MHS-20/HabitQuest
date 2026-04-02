# Observability Layer - Deployment Kubernetes


L'Observability Layer implementa lo stack completo per **monitoring**, **logging** e **distributed tracing** dell'applicazione HabitQuest. 
Segue i principi dei **Three Pillars of Observability**: Metrics, Logs e Traces.

## Three Pillars of Observability
### 1. Metrics (Prometheus)
- Misurazioni numeriche time-series (CPU, memoria, request rate, latency)  
- Prometheus scraping di `/actuator/prometheus`, metriche esposte tramite Spring Boot Actuator
- Namespace `prometheus-system`

Prometheus usa **Kubernetes Service Discovery** per trovare automaticamente i pod da scrapare:
**Annotation richieste nei pod**:
```yaml
metadata:
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/port: "8080"
    prometheus.io/path: "/actuator/prometheus"
```

### 2. Logs (Loki + Fluent Bit)
- Stream di logs presi da stdout dall'applicazione  
- Fluent Bit è presente come DaemonSet su ogni node, raccoglie i log e li invia a Loki.
- Loki è l'aggregatore di log, permette di fare raccogliere e fare query
- Namespace `logging`

**Metadati aggiunti ad ogni log**:
- `namespace`: Kubernetes namespace
- `pod`: Pod name
- `container`: Container name
- `labels`: Tutti i label Kubernetes del pod


### 3. Traces (Tempo)
- Traccia il flusso distribuito delle richieste attraverso i vari microservizi
- Spring Boot invia traces usando lo standard OpenTelemetry
- Namespace `prometheus-system`

I servizi Spring Boot inviano traces a Tempo tramite configurazione:
```yaml
env:
  - name: MANAGEMENT_OTLP_TRACING_ENDPOINT
    value: http://tempo.prometheus-system:4318/v1/traces
```

Spring Boot Micrometer produce le traces automaticamente:
1. Instrumenta le richieste HTTP
2. Propaga trace context
3. Invia spans a Tempo via OTLP

### 4. Grafana
- Visualizzazione unificata di metriche, log e traces
- Dashboard impostate per i principali aspetti dell'applicazione
- Namespace `logging`

Dashboard disponibili:
- `circuit-breaker.json`: Resilience4j circuit breakers statuses and metrics
- `http-rest-controllers.json`: REST endpoint performance and http traffic
- `jvm.json`: JVM heap, threads, GC metrics
- `logs.json`: Log aggregation of all services
- `spring-cloud-gateway.json`: Gateway routing metrics
- `traces.json`: Distributed tracing visualization

Tutti i componenti dello stack di osservabilità vengono deployati usando Helm charts parametrizzati.