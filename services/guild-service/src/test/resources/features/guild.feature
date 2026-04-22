Feature: Guild management

  Scenario: Create a guild
    Given an authenticated user
    When the user creates a guild
    Then the system registers the guild
    And assigns the Leader role to the creator

  Scenario: Invite to join a guild
    Given an existing guild
    When the leader send a join request to an avatar
    Then the system records the request

  Scenario: Accept a guild invitation
    Given a pending guild invitation
    When the avatar accepts the invitation
    Then the system adds the avatar to the guild
    And assigns the Member role to the avatar

  Scenario: Promote a guild member
    Given a guild leader
    When the leader promotes a member to Officer
    Then the system updates the member's role
