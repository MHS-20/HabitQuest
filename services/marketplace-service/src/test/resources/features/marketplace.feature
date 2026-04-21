Feature: Marketplace management

  Scenario: View all available items in the catalog
    Given an authenticated user with a marketplace
    When the user requests the list of available items
    Then the system returns all items not yet purchased

  Scenario: View available items filtered by type
    Given an authenticated user with a marketplace
    When the user requests items filtered by type
    Then the system returns only items of that type that are still available

  Scenario: Purchase item with sufficient currency and required level
    Given a user with enough in-game currency and the required level
    When the user purchases an item
    Then the system deducts the currency from the avatar
    And adds the item to the avatar's inventory
    And marks the item as sold in the marketplace
    And publishes an ItemBought event

  Scenario: Purchase item with insufficient currency
    Given a user with insufficient currency
    When the user attempts to purchase an item
    Then the system rejects the purchase
    And rolls back any partial changes

  Scenario: Purchase item with insufficient level
    Given a user whose level is below the item's required level
    When the user attempts to purchase an item
    Then the system rejects the purchase with an insufficient level error

  Scenario: Sell a previously purchased item
    Given a user who has previously purchased an item in the marketplace
    When the user sells the item back
    Then the system removes the item from the avatar's inventory
    And credits the avatar with the item's price
    And marks the item as available again in the marketplace
    And publishes an ItemSold event
