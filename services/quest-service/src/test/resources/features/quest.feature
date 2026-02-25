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
