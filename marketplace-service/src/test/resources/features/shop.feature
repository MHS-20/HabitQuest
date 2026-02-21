Feature: Shop management

  Scenario: View item catalog
    Given an authenticated user
    When the user requests the shop catalog
    Then the system returns the list of available items

  Scenario: Purchase item with sufficient currency
    Given a user with enough in-game currency
    When the user purchases an item
    Then the system deducts the currency
    And publishes an ItemPurchased event

  Scenario: Purchase item with insufficient currency
    Given a user with insufficient currency
    When the user attempts to purchase an item
    Then the system rejects the purchase
