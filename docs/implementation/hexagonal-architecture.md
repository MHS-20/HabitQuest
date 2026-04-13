# Hexagonal Architecture & DDD

In this project, the hexagonal architecture is realized through **three custom annotations** that act as semantic markers,
paired with the interfaces of the `application` package and their concrete implementations in the `infrastructure` package (adapter).

```
common/
└── hexagonal/ 
    ├── @InBoundPort
    ├── @OutBoundPort
    └── @Adapter

habitquest/tracking/
├── domain/         ← Core
├── application/    ← Ports (InBound + OutBound)
└── infrastructure/ ← Adapters
```

| Annotation | Role | Who uses it |
|---|---|---|
| `@InBoundPort` | Exposes the operations that the domain makes available to the outside world | Service interfaces (`HabitService`) |
| `@OutBoundPort` | Defines the dependencies that the domain requires from the infrastructure | Repository, notifier, logger interfaces |
| `@Adapter` | Concrete implementation of a port, connecting the domain to a specific technology | `HabitServiceImpl`, infrastructural implementations |


## Hexagonal Structure of Microservices
This architecture is systematically applied to every microservice in the project.
The pattern to follow is:

**1. InBoundPort** — one `@InBoundPort` interface for each main use-case of the service (e.g. `QuestService`, `AvatarService`), which exposes operations on the domain.
**2. OutBoundPort** — one `@OutBoundPort` interface for each external dependency:
- `XyzRepository` (persistence)
- `XyzNotifier` (messaging towards other services)
- `XyzRestClient` (HTTP calls towards other microservices)
- `XyzLogger` (decoupled logging)
**3. Adapter** — one `@Adapter @Service` implementation of the InBoundPort, plus the `@Adapter @Component` implementations of all OutBoundPorts in the infrastructural layer (Notifier, Repository, RestClient, Logger).
**4. Observer** — an `XyzObserver` interface in the domain and its `XyzObserverImpl` implementation in the application layer, which acts as a dispatcher of events towards the `XyzNotifier`.


## Integration with DDD
The entire structure relies on the **DDD marker interfaces** defined in `common.ddd`.
These interfaces add no behavior, but make the roles of types in the model explicit:

```java
public interface Aggregate<T> extends Entity<T> { T getId(); }
public interface DomainEvent extends ValueObject {}
public interface Repository {}
public interface Factory {}
public interface ValueObject {}
```

In general, there is at least one Aggregate per microservice, which allows access to all domain values and contains the core of the business logic.
Additionally, numerous Value Objects, Entities and Domain Events are present.
Domain events are all **immutable Java records** that implement the same interface, for example `HabitEvent extends DomainEvent`.
Each event carries the relevant state at the moment of its creation.

```java
public interface HabitEvent extends DomainEvent {}

public record HabitCreated(Habit habit, Id<Avatar> avatarId)   implements HabitEvent {}
public record HabitUpdated(Habit habit, Id<Avatar> avatarId)   implements HabitEvent {}
public record HabitAttended(Habit habit, Id<Avatar> avatarId)  implements HabitEvent {}
public record HabitDeleted(Id<Habit> habitId, Id<Avatar> avatarId) implements HabitEvent {}
public record HabitNotAttended(Habit habit, Id<Avatar> avatarId) implements HabitEvent {}
```