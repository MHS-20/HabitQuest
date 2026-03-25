# Notification Service

**Service:** Notification Service

The Notification Service does not expose a REST HTTP API. It is entirely **event-driven**, consuming domain events published by other services via a message broker (Kafka/RabbitMQ) and dispatching notifications to users.

---

## Event Consumers

| Consumer | Description |
|----------|-------------|
| `EventConsumer` | General-purpose domain event consumer |
| `BattleEventConsumer` | Processes battle-related events (e.g. battle started, boss defeated) |
| `HabitEventConsumer` | Processes habit completion and missed habit events |
| `AvatarAwareEventConsumer` | Processes events that require awareness of avatar state |

---

## Triggered Notifications

Notifications are dispatched in response to events such as:

- Habit completion or missed deadline
- Level-up achieved
- Guild battle started or concluded
- Guild invite received

Notification delivery channels (push notification, email, etc.) are configured per user.
