Feature: Habit management

  Scenario: Create a new habit
    Given an authenticated user
    When the user creates a habit with title, description, and recurrence
    Then the habit is stored
    And a HabitCreated event is appended to the habit history

  Scenario: Create a habit linked to a quest
    Given an authenticated user
    When the user creates a habit with an associated quest ID
    Then the habit is stored with the quest reference
    And attendance will be reported to the quest service when completed

  Scenario: Attend a habit
    Given an active habit belonging to a user
    When the user marks the habit as attended on a given date
    Then the system records the attendance date
    And grants experience to the avatar
    And publishes a HabitAttended event

  Scenario: Attend a quest-linked habit
    Given an active habit associated with a quest
    When the user marks the habit as attended
    Then the system records the attendance to the quest service

  Scenario: Missed habit detection
    Given an active habit whose next recurrence has passed without attendance
    When the scheduled job runs
    Then the system records the habit as not attended
    And applies damage to the avatar
    And publishes a HabitNotAttended event

  Scenario: Missed habit deduplication
    Given a habit that was already recorded as not attended in the last check
    When the scheduled job runs again without any new attendance
    Then the system does not apply damage again
    And does not publish a duplicate HabitNotAttended event

  Scenario: Update habit title
    Given an existing habit
    When the user updates the habit title
    Then the system stores the new title
    And appends a HabitUpdated event to the history

  Scenario: Update habit recurrence
    Given an existing habit
    When the user updates the habit recurrence
    Then the system stores the new recurrence schedule
    And appends a HabitUpdated event to the history

  Scenario: Delete a habit
    Given an existing habit
    When the user deletes the habit
    Then the habit is removed from the repository
    And a HabitDeleted event is published
