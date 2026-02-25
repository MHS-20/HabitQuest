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
