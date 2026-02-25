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
