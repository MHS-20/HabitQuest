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
