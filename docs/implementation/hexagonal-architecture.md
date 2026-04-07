# Hexagonal Architecture & DDD

In questo progetto, l'architettura esagonale viene realizzata attraverso **tre annotazioni custom** che fungono da marcatori semantici, 
abbinate alle interfacce del package `application` e alle loro implementazioni concrete nel package `infrastructure` (adapter).

```
common/
└── hexagonal/ 
    ├── @InBoundPort
    ├── @OutBoundPort
    └── @Adapter

habitquest/tracking/
├── domain/         ← Core
├── application/    ← Porte (InBound + OutBound)
└── infrastructure/ ← Adapters
```

| Annotazione | Ruolo | Chi la usa |
|---|---|---|
| `@InBoundPort` | Espone le operazioni che il dominio mette a disposizione del mondo esterno | Interfacce di servizio (`HabitService`) |
| `@OutBoundPort` | Definisce le dipendenze che il dominio richiede all'infrastruttura | Interfacce di repository, notifier, logger |
| `@Adapter` | Implementazione concreta di una porta, che collega il dominio a una tecnologia specifica | `HabitServiceImpl`, implementazioni infrastrutturali |


## Struttura Esagonale dei Microservizi
Questa architettura è sistematicamente applicata ad in ogni microservizio del progetto.
Il pattern da seguire è il seguente:

**1. InBoundPort** — un'interfaccia `@InBoundPort` per ogni use-case principale del servizio (es. `QuestService`, `AvatarService`), che espone le operazioni sul dominio.
**2. OutBoundPort** — un'interfaccia `@OutBoundPort` per ogni dipendenza esterna:
    - `XyzRepository` (persistenza)
    - `XyzNotifier` (messaggistica verso altri servizi)
    - `XyzRestClient` (chiamate HTTP verso altri microservizi)
    - `XyzLogger` (logging disaccoppiato)
**3. Adapter** — un'implementazione `@Adapter @Service` dell'InBoundPort, più le implementazioni `@Adapter @Component` di tutti gli OutBoundPort nel layer infrastrutturale (Notifier, Repository, RestClient, Logger).
**4. Observer** — un'interfaccia `XyzObserver` nel dominio e una sua implementazione `XyzObserverImpl` nel layer application, che fa da dispatcher degli eventi verso il `XyzNotifier`.


## Integrazione con il DDD
Tutta la struttura si appoggia sulle **marker interface del DDD** definite in `common.ddd`. 
Queste interfacce non aggiungono comportamento, ma rendono espliciti i ruoli dei tipi nel modello:

```java
public interface Aggregate<T> extends Entity<T> { T getId(); }
public interface DomainEvent extends ValueObject {}
public interface Repository {}
public interface Factory {}
public interface ValueObject {}
```

In generale è presente almeno un Aggregate per ogni microservizio, che permette di accedere a tutti i valori di dominio e contiene il cuore della logica di business.
In più sono presenti numerosi Value Objects, Entities e Domain Events.
Gli eventi di dominio sono tutti **Java records immutabili** che implementano una stessa interfaccia, ad esempio `HabitEvent extends DomainEvent`.
Ogni evento trasporta lo stato rilevante al momento della sua creazione.

```java
public interface HabitEvent extends DomainEvent {}

public record HabitCreated(Habit habit, Id<Avatar> avatarId)   implements HabitEvent {}
public record HabitUpdated(Habit habit, Id<Avatar> avatarId)   implements HabitEvent {}
public record HabitAttended(Habit habit, Id<Avatar> avatarId)  implements HabitEvent {}
public record HabitDeleted(Id<Habit> habitId, Id<Avatar> avatarId) implements HabitEvent {}
public record HabitNotAttended(Habit habit, Id<Avatar> avatarId) implements HabitEvent {}
```

