# Domain: Quest
A quest is a structured challenge that an Avatar can undertake:
it consists of a set of habits to be respected within a defined time window,
with a reward at stake for those who complete it.

A quest is composed of:

- **Name**: the mission title.
- **Duration**: the time window within which it must be completed (expressed in days).
- **Required habits**: the list of habits that the Avatar must respect during the quest. Each habit has a title, a description, optional tags and its own **recurrence** (daily, weekly or monthly), which determines how many times it will need to be completed over the total duration.
- **Reward**: currently modeled as a **monetary reward**.

The system automatically calculates the **total number of required completions** for each habit,
taking into account the duration and the recurrence.

## Quests & ActiveQuests
The quest exists on two distinct levels: the **quest definition** and the **active progress** of each Avatar who has joined the quest.
**The Quest (template)** is the abstract definition: it has a name, a duration, a list of required habits and a reward.
It exists independently of who is participating and their progress.
It can be undertaken by multiple Avatars simultaneously.

**ActiveQuests** is the concrete instance of a quest for a specific Avatar:
it is created when a specific Avatar joins the mission, keeps track of how many times each habit has been respected, and concludes when the mission is completed or expired.

## Joining
When an Avatar decides to participate in a quest, the system:

1. Creates an **ActiveQuests** instance dedicated to that Avatar, with the start date equal to the day they join and the end date calculated by adding the quest duration.
2. Calculates the **required occurrences** for each habit in the specified period.
3. The corresponding habits are created in the Avatar's profile, linking them to the quest. This way the Avatar will find the new habits already ready in their daily tracker.

If the Avatar has already joined the same quest previously, joining is idempotent:
the existing instance is returned without creating a new one.

## Progression & Completion
Every time the completion of a habit associated with a quest is recorded,
the attendance count in the corresponding `ActiveQuests` is updated.

The system applies some validity rules:

- An attendance is accepted only if the quest is still `IN_PROGRESS`.
- The completion date must fall within the quest's time window (not before the start date, not beyond the end date).
- Attendances in excess of the required number for that habit are not counted.

Every time an attendance is recorded, the system checks whether all habits have reached the required number of occurrences.
In that case, the quest is marked as **COMPLETED** and the `QuestCompleted` event is generated.

### Expiration
When an Avatar's progress is queried, the system automatically updates the state of active quests:
if today's date is later than the end date and the quest is still in progress, the state transitions to **EXPIRED**.
The quest expires without a reward.

| State | Meaning |
|---|---|
| `IN_PROGRESS` | The quest is active, attendances are counted |
| `COMPLETED` | All required habits have been respected within the deadline |
| `EXPIRED` | Time ran out before completion |

## Domain Events
Every significant moment in the life of a quest generates an event:

| Event | When it is generated |
|---|---|
| `QuestCreated` | At the creation of a new quest in the catalog |
| `QuestJoined` | When an Avatar joins a quest |
| `QuestCompleted` | When an Avatar completes all required habits |
| `QuestLeft` | When an Avatar abandons a quest (defined in the model, logic to be implemented) |
| `QuestNotCompleted` | Notification of an incomplete quest (defined in the model, logic to be implemented) |