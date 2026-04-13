# Domain: Marketplace
The Marketplace is the virtual shop of the HabitQuest game.
Each avatar in the game has their own personal marketplace, where they can buy and sell items — weapons, armor and potions — using the in-game currency.
The service entirely manages this lifecycle: from browsing the available catalog, to purchasing and returning items, to notifying significant events.

The marketplace does not directly manage the avatar (which belongs to another service),
but each marketplace is **uniquely associated with an avatar**:
there is no marketplace shared between multiple avatars, nor an avatar that owns more than one.

The **Marketplace** keeps track of two fundamental pieces of information:

- which items are **available** for purchase (i.e. present in the catalog but not yet bought),
- which items have been **purchased** (i.e. are in the avatar's possession).

For a given marketplace **an item can be in only one of the two states**: available or purchased.
The marketplace is created with a predefined set of purchasable items (ItemCatalog).
The distinction between "available" and "purchased" is managed by the marketplace itself, not by the catalog.

### Items
Items are **value objects**: they have no identity of their own, but are identified by their name.

Each item has:

- a **unique name** (used as a key in all operations),
- a **description**,
- a **price** in coins,
- a **minimum level required** to be able to purchase it,
- a **specific stat** that varies depending on the type.

There are no items without a level requirement.
There are three item families:

- **Weapons:** grant an attack power value.
- **Armor:** grants a defensive power value.
- **Potions:** divided into two subtypes:
  - *Health Potion* — restores hit points (HP).
  - *Mana Potion* — restores mana points (MP).

### Money
**Money** is the monetary value used in the marketplace.
It is a value object that guarantees the amount is always non-negative.
It supports addition and subtraction operations, but prevents dropping below zero (subtracting an amount greater than the available balance is an illegal operation).

## Dynamics and Business Logic

### Purchasing an Item
When an avatar wants to purchase an item, the system verifies that:

1. The item exists in the marketplace catalog.
2. The item has not already been purchased (it is not in the "sold" state).
3. The avatar has a sufficient level to purchase the item.

If both conditions are satisfied, the item transitions from the "available" state to the "purchased" state.
An item **cannot be purchased twice**: attempting to buy something already in the avatar's possession is a forbidden operation.

### Selling an Item
Selling allows the avatar to **return** a previously purchased item,
making it available again in the catalog and receiving its monetary value in exchange.

The system verifies that:

1. The item exists in the catalog.
2. The item is effectively in the "purchased" state by the avatar.

If the conditions are met, the item becomes available again.
Attempting to sell an item that is not owned is not permitted.

### Browsing the Catalog
The marketplace exposes several browsing modes:

- **All available items** — those in the catalog that have not yet been bought.
- **Available items by type** — filtering by Weapon, Armor, Potion, etc.
- **A single available item by name** — if it exists and has not been bought.
- **All purchased items** — those currently in the avatar's possession.
- **A single purchased item by name**.

## Domain Events
When relevant operations occur in the marketplace, the system emits **domain events** that inform the rest of the system of what has happened.
Events are immutable and carry all the information necessary to be processed by other services.

### ItemBought
Emitted when an avatar successfully purchases an item. Contains:

- the marketplace identifier,
- the name of the purchased item,
- the avatar identifier.

This event signals to the system that the avatar has obtained a new item and that the marketplace state has changed.

### ItemSold
Emitted when an avatar returns a previously purchased item.
Contains the same information as ItemBought (marketplace, item name, avatar).
This event signals that the item has become available again in the avatar's marketplace.

## Domain Structure
| Concept | Nature | Responsibility |
|---|---|---|
| Marketplace | Aggregate | Manages the state of items (available / purchased) for an avatar |
| ItemCatalog | Domain component | Contains the static list of all sellable items |
| Item (Weapon, Armor, Potion) | Value Object | Describes the characteristics of an item |
| Money | Value Object | Represents a non-negative monetary amount |
| Level | Value Object | Represents a game level (≥ 1) |
| Avatar | External reference | Identifies the owner of the marketplace |
| ItemBought / ItemSold | Domain events | Notify the system of completed transactions |