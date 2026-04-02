# Identity & Access

- The system must allow registration via email/password and OAuth providers.  
- The system must allow authentication via token.  
- The system must allow user profile management (username, avatar).  
- The system must allow viewing of the character’s general statistics.  

# Habit Management

- The user can create a habit by specifying title, description, difficulty, and frequency.  
- The user can define daily, weekly, or custom frequencies.  
- The user can associate one or more tags with a habit.  
- The user can configure optional penalties for non-completion.  
- The user can edit or delete a habit.  
- The user can mark a habit as completed.  

# Gamification

- The system must automatically detect non-completion at the deadline.  
- Completion or non-completion must generate domain events.  
- The system must award experience points (XP) upon habit completion.  
- Experience points must be calculated based on difficulty.  
- The system must handle level progression when XP thresholds are reached.  
- Level progression must increase the character’s statistics.  
- The system must manage statistics such as strength, constitution, intelligence, mana, and maximum health.  
- The system must reduce health points (HP) in case of a missed habit.  
- The system must maintain the character’s current state (level, XP, HP, mana, statistics).  
- The system must prevent HP or mana from dropping below zero.  
- The system must support permanent bonuses derived from equipment.  

# Notification

- The user can create reminders associated with habits.  
- The user can configure reminder time and frequency.  
- The system must send push or email notifications.  
- The system must allow modification or deletion of reminders.  
- The system must guarantee reliable scheduled execution of reminders.  

# Shop & Inventory

- The system must provide a catalog of purchasable items.  
- Items must include consumables (HP healing, mana restoration) and equipment.  
- The user can purchase items using in-game currency.  
- The user can sell owned items in exchange for currency.  
- The user can view their inventory.  
- The user can equip or remove items.  
- The user can consume items.  
- Equipped items must modify the character’s statistics.  

# Guild

- A user can create a guild.  
- A user can request to join a guild.  
- A user can be invited to a guild.  
- The system must support guild roles (leader, officer, member).  
- The system must allow member management (promotion, removal).  

# Messaging

- Guild members can send messages in the guild chat.  
- The system must provide real-time message updates.  
- The system must maintain message history.  
- The system must ensure that only guild members can access the chat.  
- The system must handle notifications for new messages.  

# Quest

- A user can create public quests by defining title, description, duration, and associated habits.  
- A user can join public quests.  
- Quest habits must be replicated into the user’s personal plan.  
- The system must track individual progress within the quest.  
- The system must assign rewards upon quest completion.  
- The system must allow the creation of private guild quests.  
- The system must prevent joining expired quests.  

# Combat

- A guild can initiate a boss fight.  
- Members can perform attacks periodically.  
- Members can use spells that consume mana.  
- The system must calculate damage based on statistics and equipment.  
- The system must update the boss’s state in real time.  
- The system must distribute rewards upon victory.  
- The system must apply penalties in case of defeat.  
- The system must prevent actions when HP or mana are insufficient.  
- The system must ensure consistency in concurrent combat action handling.  