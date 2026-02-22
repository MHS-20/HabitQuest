Feature: Experience and Level Management

  As a player
  I want my avatar to gain experience and level up
  So that I can increase my stats and unlock skill points

  Scenario: Avatar gains experience without leveling up
    Given an avatar with 80 XP and level 1
    And the level up threshold is 100 XP
    When the avatar receives 10 XP
    Then the avatar total XP should be 90
    And the avatar level should remain 1
    And no skill point should be awarded

  Scenario: Avatar levels up when reaching threshold
    Given an avatar with 90 XP and level 1
    And the level up threshold is 100 XP
    When the avatar receives 20 XP
    Then the avatar level should become 2
    And the avatar should receive 1 skill point
    And the avatar stats should increase according to level up rules

  Scenario: Avatar levels up multiple times if enough XP is gained
    Given an avatar with 90 XP and level 1
    And the next two level thresholds are 100 XP and 200 XP
    When the avatar receives 150 XP
    Then the avatar level should become 3
    And the avatar should receive 2 skill points