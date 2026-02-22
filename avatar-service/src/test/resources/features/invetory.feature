Feature: Use Consumable Items from Inventory

  As a player
  I want to use consumable items from my inventory
  So that I can restore health or mana during gameplay

  Scenario: Use health potion successfully
    Given an avatar with current health 40
    And maximum health 100
    And a health potion that restores 30 health points
    And the avatar has 1 health potion in inventory
    When the player uses the health potion
    Then the avatar current health should become 70
    And the health potion quantity should decrease by 1

  Scenario: Health cannot exceed maximum value
    Given an avatar with current health 90
    And maximum health 100
    And a health potion that restores 30 health points
    And the avatar has 1 health potion in inventory
    When the player uses the health potion
    Then the avatar current health should become 100
    And the health potion quantity should decrease by 1

  Scenario: Use mana potion successfully
    Given an avatar with current mana 20
    And maximum mana 80
    And a mana potion that restores 40 mana points
    And the avatar has 2 mana potions in inventory
    When the player uses a mana potion
    Then the avatar current mana should become 60
    And the mana potion quantity should decrease by 1

  Scenario: Cannot use potion if none available
    Given an avatar with 0 health potions in inventory
    When the player tries to use a health potion
    Then the operation should be rejected
    And the avatar health should remain unchanged

  Scenario: Cannot use potion if avatar is already at maximum health
    Given an avatar with current health 100
    And maximum health 100
    And the avatar has 1 health potion in inventory
    When the player tries to use the health potion
    Then the operation should be rejected
    And the potion quantity should remain unchanged

  Scenario: Using potion removes item if quantity reaches zero
    Given an avatar with 1 mana potion in inventory
    And current mana 10
    And maximum mana 100
    And the mana potion restores 50 mana points
    When the player uses the mana potion
    Then the avatar current mana should become 60
    And the mana potion should no longer exist in inventory