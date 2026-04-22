# ADR-0002 — Kafka domain events as the integration channel between bounded contexts and the Notification Service

| Field               | Value                                                           |
|---------------------|-----------------------------------------------------------------|
| ID                  | ADR-0002                                                        |
| **Status**          | Accepted                                                        |
| **Related ADRs**    | ADR-0001 (REST as primary inter-service communication)          |
| **Related patterns**| Domain Events · DDD · Event-Driven Architecture                 |


## 1. Context
ADR-0001 established that synchronous REST is the primary communication style between HabitQuest microservices. 
That decision covers service-to-service interactions where an immediate response is required (e.g. an orchestrator calling `avatar` to grant XP as part of a SAGA).

However, a dedicated **Notification Service** must be informed whenever a relevant domain event occurs inside any bounded context — for example:

- an avatar levels up (`LevelUp`)
- a guild invite is accepted (`GuildInviteAccepted`)
- an avatar is defeated in combat (`AvatarDefeated`)

The Notification Service is responsible exclusively for delivering emails to users. 
It does not own any domain data, does not participate in SAGAs, and never needs to reply to the originating service. 
Its only requirement is to eventually receive the event and act on it.

Extending the synchronous REST model to cover this use case would require the originating service (e.g. `avatar`) to know the address of the Notification Service and call it explicitly after completing its own domain logic. 
This introduces coupling that creates a temporal dependency on an ancillary service during the critical path of a domain operation.

### Alternatives considered

| Option | Description |
|--------|-------------|
| **A — Kafka domain events** *(chosen)* | The originating service publishes a domain event to a Kafka topic after committing its own transaction. The Notification Service consumes the topic independently and is the only consumer. |
| **B — Synchronous REST call to Notification Service** | The originating service calls the Notification Service directly via HTTP after completing its domain logic, following the same pattern used for inter-service SAGA steps. |

## 2. Decision

**Publish domain events to Kafka topics** from within each bounded context whenever a significant domain event occurs. 
The Notification Service is the sole consumer of these topics and operates entirely event-driven. No other service reads from these topics.

This is a **deliberate and bounded exception** to the synchronous REST rule established in ADR-0001: asynchronous messaging is adopted only for the one-way, fire-and-forget integration with the Notification Service. 
All other inter-service communication remains synchronous.

### Rationale

**The Notification Service has fundamentally different communication semantics.** Unlike service-to-service calls in a SAGA — where the caller needs a result to proceed — notifying a user is a side effect that requires no reply. Forcing it into a request/response model adds latency and failure surface to the originating service's critical path for no benefit.

**Bounded-context isolation.** With Kafka, the `avatar` service publishes an event (`LevelUp`) to a topic and is done. It has no knowledge of the Notification Service's existence, address, or availability. This preserves the bounded-context autonomy: the domain model is not polluted by cross-cutting notification concerns.

**Resilience and availability decoupling.** If the Notification Service is temporarily unavailable (restart, deployment, crash), events accumulate in the Kafka topic and are processed once the service recovers. With synchronous REST, a Notification Service outage would either propagate failure to the caller or require the caller to implement its own retry/circuit-breaker logic — adding complexity to every originating service.

**Natural fit for an event-driven consumer.** The Notification Service has no use for synchronous APIs: it reacts to things that happened, it never needs to ask for them. An event driven service is the most suitable representation of this contract.

---

## 3. Consequences

### Positive

- **Zero coupling between bounded contexts and the Notification Service.** Originating services need no knowledge of the consumer; adding or removing notification types requires no change to domain services.
- **Resilience by design.** Kafka retains events; the Notification Service can restart, redeploy, or lag without causing failures in the originating service.
- **Notification logic stays in one place.** All user-facing notification decisions (templates, channels, preferences) are encapsulated in the Notification Service, not scattered across bounded contexts.
- **Consistent with DDD domain events.** Domain events are first-class citizens of the model; publishing them to Kafka is a natural externalisation of an already-existing concept.
- **Architectural clarity.** The rule is simple and enforceable: REST for synchronous service-to-service calls; Kafka only for domain event fan-out to the Notification Service.

### Trade-offs

- **Operational overhead.** Kafka requires a running broker, topic provisioning, consumer group management, and monitoring — infrastructure that does not exist in the purely synchronous REST model.
- **At-least-once delivery requires idempotent consumers.** The Notification Service should handle duplicate events gracefully (e.g. deduplication by event ID) to avoid sending duplicate notifications.
- **Eventual delivery, not immediate.** Notifications may arrive with a small delay after the domain event. This is acceptable for user-facing notifications but must be documented as a deliberate trade-off.

---

## 4. Compliance

### Automated fitness functions

| Check | Tool | Description |
|-------|------|-------------|
| **No direct HTTP calls to Notification Service from domain/application layers** | ArchUnit | Verifies that no class in `..domain..` or `..application..` imports or references any HTTP client directed at the Notification Service. All notification triggers must go through the event publishing port. |
| **Domain events published only through the outbox port** | ArchUnit | Verifies that no class outside the designated outbox adapter directly instantiates a Kafka producer or calls the Kafka client API. All publishing must go through the `DomainEventPublisher` port defined in the application layer. |
| **Notification Service has no REST endpoints for domain data** | ArchUnit (on `notification` module) | Verifies that the Notification Service contains no `@RestController` or `@FeignClient` that targets another bounded context. Its only inbound channel must be Kafka consumers (`@KafkaListener`). |
| **Event payload schema compatibility** | CI pipeline — Schema Registry compatibility check | On every change to an event class, the CI pipeline runs a schema compatibility check (BACKWARD compatible minimum) against the Schema Registry before allowing the build to proceed. |

### Manual — periodic review
If a second service beyond the Notification Service needs to consume domain events, this ADR must be amended before adding that consumer: the current topology is intentionally single-consumer and the implications of a multi-consumer model (consumer group isolation, schema stability guarantees, ordering semantics) must be evaluated explicitly.