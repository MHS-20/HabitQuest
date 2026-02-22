Feature: Skill Point Allocation

  As a player
  I want to spend skill points to increase my avatar stats
  So that I can specialize my character

  Scenario: Spend skill point to increase strength
    Given an avatar with 1 available skill point
    And strength equal to 10
    When the player allocates 1 point to strength
    Then strength should become 11
    And available skill points should become 0

  Scenario: Spend skill point to increase intelligence
    Given an avatar with 2 available skill points
    And intelligence equal to 8
    When the player allocates 1 point to intelligence
    Then intelligence should become 9
    And available skill points should become 1

  Scenario: Spend skill point to increase robustness
    Given an avatar with 1 available skill point
    And robustness equal to 12
    When the player allocates 1 point to robustness
    Then robustness should become 13
    And available skill points should become 0

  Scenario: Cannot allocate skill point if none are available
    Given an avatar with 0 available skill points
    When the player tries to allocate 1 point to strength
    Then the operation should be rejected
    And strength should remain unchanged