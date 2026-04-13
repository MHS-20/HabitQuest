# Microservices Pattern with Spring Boot and Kubernetes
The architecture adopts a consolidated set of patterns to guarantee scalability, resilience, observability and operational simplicity.
Services communicate both synchronously (REST over HTTP) and asynchronously (via Apache Kafka),
and the entire infrastructure is configured to run in a Docker/Kubernetes environment,
with native support for service discovery, externalized configuration management and end-to-end monitoring.

| Pattern | Technology                                             | Goal |
|---|--------------------------------------------------------|---|
| API Gateway | Spring Cloud Gateway                                   | Single entry point, routing, rate limiting |
| Event-Driven | Apache Kafka                                           | Asynchronous communication, decoupling |
| Observability | Actuator, Micrometer, Prometheus, Loki, Tempo, Grafana | Metrics, logs and distributed traces |
| Circuit Breaker / Retry / Timeout | Spring Resilience4j                                    | Resilience and fault tolerance |
| Externalized Configuration | Kustomize, Spring profiles                             | Config/code separation, multi-environment |
| Service Discovery | Kubernetes DNS                                         | Dynamic service location |

## Patterns
### 1. API Gateway — *Spring Cloud Gateway*
The **API Gateway** acts as the single entry point for all incoming requests, preventing clients from knowing the internal topology.
This way any reorganization of the microservices is transparent to the clients, which always interact exclusively with a stable endpoint.

The gateway is built with **Spring Cloud Gateway**, a reactive framework.
Each microservice exposes its own APIs, but these are accessible exclusively through the gateway:

- **Dynamic routing**: requests are routed to the correct microservice based on the request path.
- **Centralization of routing policies**: authentication, logging of incoming requests and rate limiting are managed at gateway level via filters, avoiding duplication of logic in individual services.
- **Rate Limiting**: a custom `WebFilter` has been implemented in the gateway that limits the number of requests in a given time interval, protecting downstream services from unexpected overloads.
- **Circuit Breaker**: the gateway is also configured with Resilience4j to protect downstream services, returning fallback responses in case of degradation.

### 2. Event-Driven Architecture — *Notification Service with Apache Kafka*
A dedicated **Notification Service** has been built, acting exclusively as a **Kafka consumer**.
The other microservices publish events on specific Kafka topics whenever a relevant domain event occurs.
The Notification Service listens on these topics and, upon receiving an event, sends an **email notification** to the affected user.
This way the sending of notifications is asynchronous and does not block the control flow of the producer services.
At the same time, by centralizing notifications in a separate service, it is easy to extend or evolve the notification logic to other communication channels as well.

Communication follows the pattern below:
```
[Microservice A] --> (event on Kafka topic) --> [Kafka Broker] --> [Notification Service] --> (email)
[Microservice B] --> (event on Kafka topic) --> [Kafka Broker] --> [Notification Service] --> (email)
```

- **Temporal decoupling**: producer services do not need to wait for the notification to be delivered.
- **Resilience**: if the Notification Service is temporarily unavailable, messages remain in the Kafka topic and are processed upon recovery.
- **Scalability**: the Notification Service can be scaled horizontally independently of the other services.

---

### 3. Observability — *Kubernetes Health Checks, Spring Actuator, Micrometer, Prometheus, Loki, Tempo*

**Context and motivation**

In a distributed system it is essential to have complete visibility into the state of the system at all times.
**Observability** is traditionally organized into three pillars: **metrics**, **logs** and **distributed traces**.

The observability stack has been built on multiple complementary layers:

1. **Health Checks with Kubernetes**: Each microservice exposes the `/actuator/health/liveness` and `/actuator/health/readiness` endpoints via Spring Actuator.
   Kubernetes uses these endpoints to configure **liveness probes** and **readiness probes**,
   ensuring that traffic is routed only to actually operational instances and that non-functioning containers are automatically restarted.

2. **Metrics with Spring Micrometer and Prometheus**: Spring Micrometer acts as an abstraction layer for the collection of application metrics.
   Metrics are exposed in the **Prometheus**-compatible format via the `/actuator/prometheus` endpoint.

3. **Aggregated logs with Loki and FluentBit**: Logs produced by all microservices are collected and aggregated in **Loki**, allowing searches across logs of different services on a single interface.

4. **Distributed traces with Tempo**: **Distributed telemetry** is enabled via integration with **Tempo**: each HTTP request generates a `trace ID` that is propagated across all involved microservices.
   This allows the complete end-to-end path of a request to be reconstructed.


### 4. Circuit Breaker, Timeout and Retry — *Spring Resilience4j*

The **Circuit Breaker**, **Timeout** and **Retry** patterns collaborate to isolate faults and increase failure tolerance.
**Spring Resilience4j** has been integrated in all microservices and in the gateway. Each HTTP call between services is protected by a combination of:

- **Circuit Breaker**: monitors calls towards a downstream service. When the error percentage exceeds a configured threshold,
  the circuit opens and subsequent requests are immediately rejected with a fallback, without waiting for a timeout.
  After a configured interval, the circuit enters a "half-open" state and tests whether the service has recovered.

- **Timeout**: each HTTP call is subject to a maximum timeout. If the downstream service does not respond within the established time,
  the call is interrupted and a fallback response is returned, preventing the caller's threads from remaining blocked indefinitely.

- **Retry**: in case of transient errors, calls are automatically retried a configured number of times, with a backoff interval between one attempt and the next.

The configuration is applied both to communications **between microservices**
(synchronous REST calls) and between the **gateway and the microservices**, ensuring that the entry point is also protected.

- **Fail-fast**: clients receive immediate responses instead of waiting for long timeouts.
- **Fault isolation**: the malfunction of a single service does not propagate to the entire system.
- **Auto-healing**: the circuit closes automatically when the service becomes available again.

Retries are configured as follows:

- A request is attempted 3 times in total
- After the first failure, 500ms are waited
- The wait time doubles for each new failed attempt.
```
resilience4j:
  retry:
    instances:
      avatarClient:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
        retry-exceptions:
          - org.springframework.web.client.RestClientException
          - java.io.IOException
          - habitquest.guild.infrastructure.AvatarCommunicationException
```

CircuitBreakers are configured as follows:

- **Sliding Window**: Analyzes the last 10 calls.
- **Failure Threshold**: If 50% of calls in the window fail, the circuit switches to OPEN state.
- **Slowness Management**: If 80% of calls take more than 3 seconds, the circuit opens.
- **OPEN State**: The circuit remains open for 10 seconds. During this time, every request fails instantly.
- **HALF-OPEN State**: After 10 seconds, the system allows 3 test calls. If these succeed, the circuit returns to CLOSED; otherwise it returns to OPEN.
```
  circuit-breaker:
    instances:
      avatarClient:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10
        failure-rate-threshold: 50
        slow-call-rate-threshold: 80
        slow-call-duration-threshold: 3s
        wait-duration-in-open-state: 10s
        permitted-calls-in-half-open-state: 3
        minimum-number-of-calls: 5
```


### 5. Externalized Configuration — *Kustomize and Spring `application.yml`*
Configuration must not be hardcoded in the source code nor in Docker images.
The **Externalized Configuration** pattern allows the configuration to be separated from the application,
making it possible to deploy the same artifact in different environments (development, staging, production) without code changes.

Configuration management is structured on two levels:

1. **Application level — Spring `application.yml`**: Each microservice defines its own configuration in `application.yml` files.
   Sensitive or environment-dependent variables (e.g. URLs) are parameterized via environment variables, which are injected at runtime by the Kubernetes layer.

2. **Infrastructure level — Kustomize**: Kustomize manages the customization of Kubernetes manifests for different environments without duplicating files.
   Through a structure of `base` and `overlays`, it is possible to define a common configuration and apply specific patches for each environment (e.g. number of replicas, CPU/memory resources, environment variables).
   This approach eliminates the need to create new manifests from scratch for each deployment environment.


### 6. Service Discovery — *Kubernetes DNS*
Kubernetes provides **native Service Discovery** via its internal DNS system.
Each microservice is associated with a Kubernetes `Service` object, which acts as a stable endpoint and is automatically registered in the cluster's DNS.
When a microservice needs to communicate with another, it uses the **DNS name of the Service** instead of an IP address.