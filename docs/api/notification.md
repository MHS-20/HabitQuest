# Notification Service
The Notification Service does not expose a REST HTTP API.
It is entirely **event-driven**, consuming domain events published by other services via a message broker (Kafka) and dispatching notifications to users.

## Event Consumers
| Consumer | Description |
|----------|-------------|
| `AvatarEventConsumer` | Processes avatar-related events (e.g. level-up, spell unlocked) |
| `BattleEventConsumer` | Processes guild battle events (e.g. battle started, battle won/lost) |
| `GuildEventConsumer` | Processes guild lifecycle and membership events (e.g. invites, role assignments) |
| `HabitEventConsumer` | Processes habit completion and missed habit events |
| `MarketplaceEventConsumer` | Processes marketplace events (e.g. item bought, item sold) |
| `QuestEventConsumer` | Processes quest events (e.g. quest created/completed, joined/left) |
| `UserEventConsumer` | Processes user onboarding events (e.g. user registered) |


## Triggered Notifications
Notifications are dispatched in response to events such as:
- Habit completion or missed deadline
- Level-up achieved
- Guild battle started or concluded
- Guild invite received
- Marketplace item bought or sold
- Quest created, completed, joined, or left
- User registered

Notification delivery channel is email, but it can be easily extended to support additional channels by implementing new notification handlers without modifying existing event consumers, nor the producers of the events.
