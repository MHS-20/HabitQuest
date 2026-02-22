## login.feature

```gherkin
Feature: User registration and authentication

  Scenario: Successful user registration
    Given a non-registered user
    When the user provides a valid email and password
    Then the system creates a new account
    And returns a valid authentication token

  Scenario: Successful login
    Given a registered user
    When the user provides correct credentials
    Then the system returns a valid JWT token

  Scenario: Login with invalid credentials
    Given a registered user
    When the user provides an incorrect password
    Then the system denies authentication
```

## combat.feature

```gherkin
Feature: Guild boss fight

  Scenario: Start a boss fight
    Given an active guild
    When the guild leader starts a boss fight
    Then the system creates a boss with initial HP

  Scenario: Attack the boss
    Given an active boss fight
    When a guild member performs an attack
    Then the system calculates the damage
    And reduces the boss's HP

  Scenario: Cast a spell
    Given a guild member with sufficient mana
    When the member casts a spell
    Then the system consumes mana
    And applies damage or effects

  Scenario: Defeat the boss
    Given a boss with zero HP
    When the system verifies the state
    Then it publishes a BossDefeated event
    And distributes rewards to guild members
```

## guild.feature

```gherkin
Feature: Guild management

  Scenario: Create a guild
    Given an authenticated user
    When the user creates a guild
    Then the system registers the guild
    And assigns the Leader role to the creator

  Scenario: Request to join a guild
    Given an existing guild
    When a user submits a join request
    Then the system records the request

  Scenario: Promote a guild member
    Given a guild leader
    When the leader promotes a member to Officer
    Then the system updates the member's role
```

## messaging.feature

```gherkin
Feature: Guild chat system

  Scenario: Send a message
    Given an authenticated guild member
    When the member sends a chat message
    Then the system stores the message
    And broadcasts it in real time to other guild members

  Scenario: Unauthorized chat access
    Given a user who is not a guild member
    When the user attempts to access the guild chat
    Then the system denies access
```

## shop.feature

```gherkin
Feature: Marketplace management

  Scenario: View item catalog
    Given an authenticated user
    When the user requests the shop catalog
    Then the system returns the list of available items

  Scenario: Purchase item with sufficient currency
    Given a user with enough in-game currency
    When the user purchases an item
    Then the system deducts the currency
    And publishes an ItemPurchased event

  Scenario: Purchase item with insufficient currency
    Given a user with insufficient currency
    When the user attempts to purchase an item
    Then the system rejects the purchase

  Scenario: Sell an item successfully
    Given an authenticated user
    And the user has an item in their inventory
    When the user selects the item and chooses to sell it
    Then the system removes the item from the inventory
    And the system credits the user with the corresponding in-game currency
    And publishes an ItemSold event
```

## quest.feature

```gherkin
Feature: Quest management

  Scenario: Create a public quest
    Given an authenticated user
    When the user creates a quest with duration and associated habits
    Then the system publishes the quest

  Scenario: Join a quest
    Given an active quest
    When a user joins the quest
    Then the system adds the quest habits to the user's habit plan

  Scenario: Complete a quest
    Given a quest whose objectives are fulfilled
    When the quest duration ends
    Then the system distributes the rewards
    And publishes a QuestCompleted event
```

## gamification.feature

```gherkin
Feature: Character progression system

  Scenario: Gain experience after habit completion
    Given a HabitCompleted event is received
    When the system calculates experience based on difficulty
    Then the character's XP increases

  Scenario: Level up when XP threshold is reached
    Given a character with sufficient XP
    When the XP exceeds the current level threshold
    Then the system increases the character's level
    And increases the base statistics
    And publishes a LevelUp event

  Scenario: Apply penalty after missed habit
    Given a HabitMissed event is received
    When the system applies the penalty
    Then the character's HP decreases
    And HP does not go below zero
```

## habit.feature

```gherkin
Feature: Habit management

  Scenario: Create a new habit
    Given an authenticated user
    When the user creates a habit with title, frequency, and difficulty
    Then the habit is stored
    And it becomes active in the user's habit plan

  Scenario: Complete a habit
    Given an active habit in the user's plan
    When the user marks the habit as completed
    Then the system records the completion
    And publishes a HabitCompleted event

  Scenario: Missed habit detection
    Given an active habit whose time window has expired
    When the system performs the scheduled check
    Then it records the habit as missed
    And publishes a HabitMissed event
```

