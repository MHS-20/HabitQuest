# Event Sourcing Applied to Habit History
In the context of HabitQuest, the EventSourcing pattern is applied in **append-only** form to the `HabitHistoryEvent` object,
which tracks every relevant operation performed on a habit.
Each habit carries with it the complete trace of what has happened to it: creation, attendance, modifications, etc.
The history is immutable and grows over time, providing a complete audit trail.

### The base event: `HabitEvent`
```java
public interface HabitEvent extends DomainEvent {}
```
`HabitEvent` extends `DomainEvent`, which in turn extends `ValueObject`.
All events are implemented as `Record` and are therefore immutable.

Five concrete events are defined:

| Event | Payload | Meaning |
|---|---|---|
| `HabitCreated` | `Habit`, `Id<Avatar>` | A habit has been created |
| `HabitUpdated` | `Habit`, `Id<Avatar>` | Title, description, tags or recurrence have changed |
| `HabitAttended` | `Habit`, `Id<Avatar>` | The avatar has marked the habit as completed |
| `HabitNotAttended` | `Habit`, `Id<Avatar>` | The habit has expired without being completed |
| `HabitDeleted` | `Id<Habit>`, `Id<Avatar>` | The habit has been deleted |
Note that `HabitDeleted` carries only the IDs and not the entire `Habit` object, because at the time of deletion the entity is no longer available.

### The wrapper: `HabitHistoryEvent`
```java
public record HabitHistoryEvent(HabitEvent event, LocalDateTime occurredAt, String details) {}
```
`HabitHistoryEvent` is the **log entry**. It wraps a `HabitEvent` adding:

- `occurredAt` — the precise timestamp of the occurrence
- `details` — a free string with contextual metadata

---

## Event Production
Event production happens entirely in `HabitServiceImpl`, via the private method `appendHistory`:

```java
private void appendHistory(HabitEvent event, String details) {
  historyRepository.append(new HabitHistoryEvent(event, LocalDateTime.now(), details));
}
```

Every domain operation calls `appendHistory` **after** completing the modification to the current state,
ensuring that the history reflects only successfully completed operations.

### Map of Operations and Emitted Events
| Operation | Emitted event | `details` |
|---|---|---|
| `createDailyHabit` | `HabitCreated` | `"daily recurrence"` |
| `createWeeklyHabit` | `HabitCreated` | `"weekly recurrence day=<day>"` |
| `createMonthlyHabit` | `HabitCreated` | `"monthly recurrence day=<day>"` |
| `updateTitle` | `HabitUpdated` | `"title=<value>"` |
| `updateDescription` | `HabitUpdated` | `"description updated"` |
| `updateTags` | `HabitUpdated` | `"tags updated count=<n>"` |
| `updateRecurrence` | `HabitUpdated` | `"recurrence=<type>"` |
| `attendHabit` | `HabitAttended` | `"attendedAt=<timestamp>"` |
| `deleteHabitById` | `HabitDeleted` | `"habit deleted"` |
| `detectOverdueHabits` | `HabitNotAttended` | `"never-attended"` or `"expectedAt=<timestamp>"` |


## The Repository: Append-Only
The `HabitHistoryRepository` interface defines a deliberately restricted contract:
```java
void append(HabitHistoryEvent event);
List<HabitHistoryEvent> findByHabitId(Id<Habit> habitId);
```

There are no `update`, `delete` or `save` methods.
The history is **write-progressive and read-only**: no event can be removed or modified after being written.
The current implementation is in-memory (`InMemoryHabitHistoryRepository`), with a `ConcurrentHashMap` that associates each `Id<Habit>` with its own list of events:
```java
private final Map<Id<Habit>, List<HabitHistoryEvent>> store = new ConcurrentHashMap<>();

@Override
public void append(HabitHistoryEvent event) {
  store.computeIfAbsent(event.habitId(), unused -> new ArrayList<>()).add(event);
}

@Override
public List<HabitHistoryEvent> findByHabitId(Id<Habit> habitId) {
  return List.copyOf(store.getOrDefault(habitId, List.of()));
}
```

`List.copyOf` ensures that the caller receives an immutable defensive copy, preventing accidental modifications to the internal list.

---

## Deduplication of `HabitNotAttended` Events
An interesting aspect is the handling of non-attendance events. The `detectOverdueHabits` method is called periodically by a scheduler; without a guard, each execution would produce a new `HabitNotAttended` for the same already-overdue habit.
The `appendNotAttendedHistoryIfNew` method solves the problem by verifying that the last recorded event is not already a `HabitNotAttended` with the same `details`:

```java
private void appendNotAttendedHistoryIfNew(Habit habit, String marker, HabitNotAttended event) {
  List<HabitHistoryEvent> history = historyRepository.findByHabitId(habit.getId());
  HabitHistoryEvent last = history.isEmpty() ? null : history.getLast();
  if (last == null
      || !(last.event() instanceof HabitNotAttended)
      || !Objects.equals(last.details(), marker)) {
    appendHistory(event, marker);
  }
}
```

This deduplication logic is necessary precisely because events are never updated: the only way to avoid duplicates is to not insert them in the first place.



## Exposure via API
The history is exposed through the `GET /api/v1/habits/{id}/history` endpoint, which returns the list of events serialized via `HabitHistoryEventResponse`:

```java
public record HabitHistoryEventResponse(
    String eventType,
    String habitId,
    String avatarId,
    LocalDateTime occurredAt,
    String details
) {}
```
The `eventType` field is the simple class name of the event (`"HabitCreated"`, `"HabitAttended"`, etc.).
The `history` link is included in the HATEOAS response of each habit, both at creation and at retrieval.