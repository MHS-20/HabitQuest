# Event sourcing applicato alla History delle Habits
Nel contesto di HabitQuest, il pattern di EventSourcing è applicato in forma **append-only** all'oggetto `HabitHistoryEvent`, 
che traccia ogni operazione rilevante eseguita su un'abitudine.
Ogni abitudine porta con sé l'intera traccia di ciò che le è accaduto: creazione, frequentazione, modifiche etc.
La storia è immutabile e cresce nel tempo, fornendo un audit trail completo.

### L'evento base: `HabitEvent`
```java
public interface HabitEvent extends DomainEvent {}
```
`HabitEvent` estende `DomainEvent`, che a sua volta estende `ValueObject`. 
Tutti gli eventi sono implementati come `Record` e quindi immutabili.

Sono definiti cinque eventi concreti:

| Evento | Payload | Significato |
|---|---|---|
| `HabitCreated` | `Habit`, `Id<Avatar>` | Un'abitudine è stata creata |
| `HabitUpdated` | `Habit`, `Id<Avatar>` | Titolo, descrizione, tag o ricorrenza sono cambiati |
| `HabitAttended` | `Habit`, `Id<Avatar>` | L'avatar ha segnato l'abitudine come completata |
| `HabitNotAttended` | `Habit`, `Id<Avatar>` | L'abitudine è scaduta senza essere completata |
| `HabitDeleted` | `Id<Habit>`, `Id<Avatar>` | L'abitudine è stata eliminata |
Da notare che `HabitDeleted` porta solo gli ID e non l'oggetto `Habit` intero, perché al momento della cancellazione l'entità non è più disponibile.

### Il wrapper: `HabitHistoryEvent`
```java
public record HabitHistoryEvent(HabitEvent event, LocalDateTime occurredAt, String details) {}
```
`HabitHistoryEvent` è la **voce del log**. Avvolge un `HabitEvent` aggiungendo:

- `occurredAt` — il timestamp preciso dell'occorrenza
- `details` — una stringa libera con metadati contestual

---

## Produzione degli eventi
La produzione degli eventi avviene interamente in `HabitServiceImpl`, tramite il metodo privato `appendHistory`:

```java
private void appendHistory(HabitEvent event, String details) {
  historyRepository.append(new HabitHistoryEvent(event, LocalDateTime.now(), details));
}
```

Ogni operazione di dominio chiama `appendHistory` **dopo** aver completato la modifica sullo stato corrente, 
garantendo che la history rifletta solo operazioni andate a buon fine.

### Mappa delle operazioni e degli eventi emessi
| Operazione | Evento emesso | `details` |
|---|---|---|
| `createDailyHabit` | `HabitCreated` | `"daily recurrence"` |
| `createWeeklyHabit` | `HabitCreated` | `"weekly recurrence day=<giorno>"` |
| `createMonthlyHabit` | `HabitCreated` | `"monthly recurrence day=<giorno>"` |
| `updateTitle` | `HabitUpdated` | `"title=<valore>"` |
| `updateDescription` | `HabitUpdated` | `"description updated"` |
| `updateTags` | `HabitUpdated` | `"tags updated count=<n>"` |
| `updateRecurrence` | `HabitUpdated` | `"recurrence=<tipo>"` |
| `attendHabit` | `HabitAttended` | `"attendedAt=<timestamp>"` |
| `deleteHabitById` | `HabitDeleted` | `"habit deleted"` |
| `detectOverdueHabits` | `HabitNotAttended` | `"never-attended"` o `"expectedAt=<timestamp>"` |


## Il repository: append-only
L'interfaccia `HabitHistoryRepository` definisce un contratto volutamente ristretto:
```java
void append(HabitHistoryEvent event);
List<HabitHistoryEvent> findByHabitId(Id<Habit> habitId);
```

Non esistono metodi `update`, `delete` o `save`. 
La storia è **solo in scrittura progressiva e in lettura**: nessun evento può essere rimosso o modificato dopo essere stato scritto.
L'implementazione corrente è in memoria (`InMemoryHabitHistoryRepository`), con una `ConcurrentHashMap` che associa ogni `Id<Habit>` alla propria lista di eventi:
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

`List.copyOf` garantisce che il chiamante riceva una copia difensiva immutabile, impedendo modifiche accidentali alla lista interna.

---

## Deduplicazione degli eventi `HabitNotAttended`
Un aspetto interessante è la gestione degli eventi di mancata partecipazione. Il metodo `detectOverdueHabits` viene chiamato periodicamente da uno scheduler; senza una guardia, ogni esecuzione produrrebbe un nuovo `HabitNotAttended` per la stessa abitudine già in ritardo.
Il metodo `appendNotAttendedHistoryIfNew` risolve il problema verificando che l'ultimo evento registrato non sia già un `HabitNotAttended` con lo stesso `details`:

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

Questa logica di deduplicazione è necessaria proprio perché gli eventi non vengono mai aggiornati: l'unico modo per evitare duplicati è non inserirli in primo luogo.



## Esposizione via API
La history è esposta attraverso l'endpoint `GET /api/v1/habits/{id}/history`, che restituisce la lista degli eventi serializzata tramite `HabitHistoryEventResponse`:

```java
public record HabitHistoryEventResponse(
    String eventType,
    String habitId,
    String avatarId,
    LocalDateTime occurredAt,
    String details
) {}
```
Il campo `eventType` è il nome semplice della classe dell'evento (`"HabitCreated"`, `"HabitAttended"`, ecc.). 
Il link `history` è incluso nella risposta HATEOAS di ogni abitudine, sia alla creazione che alla lettura.
