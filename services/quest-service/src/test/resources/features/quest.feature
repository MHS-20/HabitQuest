Feature: Quest management

  Scenario: Create a quest
    Given an authenticated user
    When the user creates a quest with a name and a duration in days
    Then the system persists the quest with a default money reward of 100
    And publishes a QuestCreated event

  Scenario: Delete a quest
    Given an existing quest
    When the user deletes the quest
    Then the system removes the quest permanently
    And returns a success response

  Scenario: Add a habit to a quest
    Given an existing quest
    When the user adds a habit with a recurrence rule to the quest
    Then the system appends the habit to the quest's habit list
    And returns a success response

  Scenario: Join a quest
    Given an existing quest with at least one habit
    When an avatar joins the quest
    Then the system creates an ActiveQuests record in IN_PROGRESS status
    And adds the quest habits to the avatar's habit tracking plan
    And publishes a QuestJoined event

  Scenario: Join a quest that the avatar has already joined
    Given an avatar who has already joined a quest
    When the avatar joins the same quest again
    Then the system returns the existing ActiveQuests record without creating a duplicate

  Scenario: Record habit attendance that completes the quest
    Given an avatar participating in a quest
    And the avatar has attended all required habit occurrences except one
    When the avatar records the final habit attendance
    Then the system marks the ActiveQuests as COMPLETED
    And grants the money reward to the avatar via the avatar service
    And publishes a QuestCompleted event

  Scenario: Record habit attendance without completing the quest
    Given an avatar participating in a quest with multiple required habit occurrences
    When the avatar records a habit attendance that does not yet fulfill all requirements
    Then the system increments the attended occurrences
    And the ActiveQuests status remains IN_PROGRESS

  Scenario: Record habit attendance after the quest window has elapsed
    Given an avatar whose active quest window has ended without completion
    When the system refreshes the quest status
    Then the ActiveQuests status transitions to EXPIRED
