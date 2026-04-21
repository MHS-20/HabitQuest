Feature: Character progression system

  Scenario: Gain experience after habit completion
    Given a HabitCompleted event is received
    When the system calculates experience based on difficulty
    Then the character's XP increases

  Scenario: Apply penalty after missed habit
    Given a HabitMissed event is received
    When the system applies the penalty
    Then the character's HP decreases
    And HP does not go below zero
