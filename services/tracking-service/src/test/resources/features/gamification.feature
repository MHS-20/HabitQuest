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
