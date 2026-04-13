# Domain: Habit
A **Habit** represents an action that a user commits to performing with a certain regularity.
Each habit belongs to an **Avatar** and has the following characteristics:

- **Title** and **Description**
- **Recurrence**: the frequency with which the habit must be performed.
- **Tags**: optional labels to categorize the habit.
- **Last completion date**: keeps track of when the habit was last respected.
- **Associated Quest** _(optional)_: a link to a specific quest in the Quest system.

Title, description, tags and recurrence can be updated at any time.

## Recurrence
Each habit has a **recurrence** that defines how often it must be completed.
There are three types:

- **Daily**: the habit must be completed every day.
- **Weekly**: the habit must be completed once a week, on a specific day.
- **Monthly**: the habit must be completed once a month, on a specific day of the month.

The recurrence allows the system to automatically calculate when a habit should have been performed and detect any **missed habits**.

## Completion
When a user respects their habit, the system records the completion date.
This triggers two side effects towards other services:

1. **The Avatar gains experience points**.
2. **If there is an associated Quest**, the system notifies the Quest Service by recording the Avatar's attendance to the habit on that date. The Quest Service can then update the mission progress.

A habit can be deleted.
The deletion event is recorded in the.

## Detection of Overdue Habits
An automatic component checks **every minute** all active habits and verifies whether any are **overdue**,
meaning habits whose expected deadline has already passed but have not yet been completed.

The conditions that make a habit "overdue" are:
- It has never been completed (the user has never respected it since they created it).
- The next expected deadline, calculated based on the last completion and the recurrence, has already passed.

When an overdue habit is detected, the system generates a dedicated event.
However, it avoids duplicating the report: if the habit has already been marked as overdue with the same information, no new event is generated.

## The History
Each habit maintains a **historical log** of all events that have concerned it, with the exact moment and a textual detail (e.g. the updated value, the chosen recurrence type, the completion date).
This history can be consulted both for a single habit and aggregated by Avatar, in reverse chronological order (most recent first).

At the time of creation, the event is recorded in the history.
Every modification of title, description or tags, or even the deletion of the habit, generates an event in the history.
Every completion or missed completion generates an event in the history.

## Domain Events
Every significant event that happens to a habit is signaled in the domain.
Events are both recorded in the habit's **internal history**
and published on a **notification channel**.

| Event | When it is generated | Is it published externally? |
|---|---|---|
| `HabitCreated` | At habit creation | No (history only) |
| `HabitUpdated` | At every modification of title, description, tags or recurrence | No (history only) |
| `HabitAttended` | When the user completes the habit | ✅ Yes |
| `HabitNotAttended` | When the system detects the overdue habit | ✅ Yes |
| `HabitDeleted` | When the habit is deleted | ✅ Yes |