# ADR-0001 — Synchronous REST as primary integration style in the HabitQuest microservice platform

| Field    | Value                                        |
|----------|----------------------------------------------|
| ID      | ADR-0001                                     |
| **Status** | Accepted                                   |
| **Related patterns** | HATEOAS · CQRS · SAGA · DDD    |


## 1. Context
HabitQuest is a gamified habit-tracking platform decomposed into independently deployable microservices,
each owning its bounded context as mandated by Domain-Driven Design.

At the start of the project the team had to select a primary inter-service communication style. The platform must support:

- Fine-grained operations on domain aggregates (create avatar, equip item, apply damage, grant XP, …).
- Complex, multi-step workflows that span more than one service (e.g. completing a quest triggers XP gain, currency drop, and achievement unlock simultaneously).

### Alternatives considered

| Option | Description |
|--------|-------------|
| **A — Synchronous REST + HATEOAS** *(chosen)* | Each service exposes a versioned HTTP API. Clients discover available transitions via hypermedia links embedded in responses. SAGA orchestrates multi-service workflows. |
| **B — Async messaging (e.g. Kafka)** | Services communicate exclusively through domain events on a broker. Consumers react to events independently. Higher operational complexity; eventual consistency by default. |

## 2. Decision

**Adopt synchronous REST with HATEOAS as the primary integration style** across all HabitQuest microservices, 
applying CQRS to segregate read and write endpoints within each service, and SAGA (orchestration variant) to manage multi-step cross-service workflows.

### Rationale
**Fit with CQRS.** Separating command and query controllers (`AvatarCommandController` vs `AvatarQueryController`) maps naturally to REST: commands mutate state via `POST` / `PATCH` / `DELETE` and return `204` or a creation response, while queries expose `GET` endpoints for individual sub-resources (`/health`, `/inventory`, `/stats`, …). This produces a uniform, self-describing API surface aligned with the CQRS read/write segregation.
**Fit with HATEOAS.** The `AvatarResponseAssembler` embeds typed hypermedia links (`earn`, `spend`, `damage`, `useHealthPotion`, …) directly in each response. Clients navigate the API by following links, eliminating hard-coded URL coupling and making the allowed state transitions explicit at runtime — a capability that async messaging cannot provide natively.
**Fit with DDD bounded contexts.** Each service owns its domain model and exposes it through a well-defined HTTP surface. REST resources map 1-to-1 to aggregate roots (`Avatar`, `Guild`, `Invite`), preserving context integrity without leaking domain internals across service boundaries.
**Fit with SAGA.** Long-running workflows that span services (e.g. quest completion → avatar XP → reward unlock) are coordinated by an orchestrator that calls each participant service synchronously, collects results, and issues compensating requests on failure. This gives deterministic, observable transaction progress — much easier to debug than choreography-based event chains.
**Team and project constraints.** The team has more experience with Spring MVC and prefer a simpler solution where services can be developed and tested in isolation without needing a running message broker or schema registry. The expected traffic volume is moderate, so the latency overhead of synchronous calls is acceptable for now.


## 3. Consequences
### Positive

- **Uniform, self-describing API surface.** HATEOAS links make state transitions discoverable without out-of-band documentation; clients never hard-code URLs.
- **Strong request/response traceability.** Each call produces an HTTP trace, making debugging and distributed tracing (e.g. via OpenTelemetry / Spring Sleuth) straightforward.
- **Clear CQRS separation enforced at the boundary.** Command and query paths are separated at the controller level, preventing accidental side-effects in read paths.
- **Simpler local development.** Services can be run and tested individually without a running broker or schema registry.
- **Explicit SAGA compensations.** Compensating transactions are synchronous and traceable, reducing the risk of phantom partial states in domain aggregates.

### Trade-offs

- **Temporal coupling.** A caller blocks until the callee responds; a slow or unavailable downstream service propagates latency and failure upstream.
- **No built-in fan-out.** Broadcasting a domain event to multiple consumers requires explicit orchestration or a secondary notification mechanism.
- **SAGA orchestrator as a coordination bottleneck.** It must be made resilient (retries, idempotency tokens, timeouts) or overall workflow reliability degrades.
- **Scalability ceiling.** Under very high throughput, synchronous HTTP call chains are harder to scale than decoupled event-driven pipelines. This trade-off is acceptable at the current traffic level but must be re-evaluated as the platform grows.

### Manual — periodic review
If the **P99 latency on SAGA-orchestrated workflows exceeds 800 ms**, or a fan-out use case emerges that cannot be satisfied by the synchronous orchestrator, 
the team will re-evaluate introducing a message broker for that specific interaction and amend this ADR accordingly.