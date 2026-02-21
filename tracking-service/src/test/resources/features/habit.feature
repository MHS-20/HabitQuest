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
