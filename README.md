# HabitQuest – Gamified Habit Tracker

## Project Overview
HabitQuest is a gamified habit tracker with RPG mechanics. Users can create habits, earn XP, level up, join guilds, do quests, and fight bosses.

<p align="center">
<img src="docs/Logo.png" alt="Logo" width="300"/>
</p>

## Features

### Habit Management
- Create, edit, and delete habits
- Configure difficulty, frequency, and tags
- Habit reminders
- XP gain for completed habits
- HP loss for missed habits

### Gamification
- Character stats: strength, constitution, intelligence, HP, mana
- Level progression
- Equipment bonuses

### Shop & Inventory
- Buy items and equipment
- Equip items to improve stats
- Use consumables (HP/mana restore)

### Guilds
- Create or join guilds
- Guild roles: Leader, Officer, Member
- Guild chat (real-time)
- Cooperative boss fights

### Quests
- Create/join quests
- Track quest progress
- Earn rewards

### Combat System
- Guild boss fights
- Attacks and magic consume resources
- Damage calculated from stats and equipment

---

## Architecture
- **Microservices**: Identity, Tracking, Notification, Avatar, Shop, Guild, Quest
- **Databases**: one per service
- **Communication**:
  - REST/gRPC for synchronous requests
  - Event-driven async via message broker
  - WebSocket for real-time chat and combat
- **DDD**: Aggregates, entities, value objects, domain events

---
## Tech Stack
- Backend: Spring Boot
- DB: PostgreSQL
- Event Bus: Kafka
- Auth: OAuth2
- UI: Kotlin multiplatform 
- Testing: Gherkin
- Deployment: Kubernetes