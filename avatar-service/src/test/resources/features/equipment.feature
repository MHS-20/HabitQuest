Feature: Equipment Management

  As a player
  I want to equip or remove weapons and armor
  So that my avatar stats change accordingly

  Scenario: Equip a weapon
    Given an avatar with strength equal to 10
    And a weapon that grants +3 strength
    When the player equips the weapon
    Then the avatar strength should become 13
    And the weapon should be marked as equipped

  Scenario: Equip an armor
    Given an avatar with robustness equal to 12
    And an armor that grants +5 robustness
    When the player equips the armor
    Then the avatar robustness should become 17
    And the armor should be marked as equipped

  Scenario: Remove equipped weapon
    Given an avatar with a weapon equipped that grants +3 strength
    And base strength equal to 10
    When the player removes the weapon
    Then the avatar strength should return to 10
    And the weapon should no longer be equipped

  Scenario: Cannot equip two weapons in the same slot
    Given an avatar with a weapon equipped in the main hand slot
    When the player tries to equip another weapon in the same slot
    Then the operation should be rejected
    And the original weapon should remain equipped