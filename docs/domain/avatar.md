# Domain: Avatar
The Avatar is the heart of the project.
It represents the game character associated with each user: an avatar is an entity that grows, fights, gains experience, accumulates items and learns spells as the user interacts with the application.
The avatar encapsulates precise business rules that determine what can happen to the character, under what conditions and with what consequences.

## Avatar Structure
At creation, each avatar starts with a well-defined initial state:

| Attribute | Initial value |
|---|---|
| Level | 1 |
| Experience | 0 / 100 |
| Health | 100 / 100 |
| Mana | 50 / 50 |
| Money | 100 coins |
| Stats (Strength / Defense / Intelligence) | 10 / 10 / 10 |
| Inventory | empty |
| Equipped items | none |
| Known spells | none |

## Progression: Experience and Level
The avatar accumulates **experience** (XP) by performing activities in the game.
When the experience reaches or exceeds the threshold required for the current level, a **level up** occurs automatically.
The experience threshold required to level up grows with each advancement: going from level 1 to 2 requires 100 XP, but the next threshold doubles each time, making progression increasingly challenging.

When the avatar levels up, three things happen automatically:

- **Maximum health** increases by 10 points.
- **Maximum mana** increases by 5 points.
- The avatar receives a bonus of **100 coins**.

Additionally, at certain specific levels the character **automatically learns a new spell** (see Spells section).


## Health and Death
**Health** represents the character's vitality.
It has a current value and a maximum value.
The character can receive damage (which lowers current health) or be healed (which restores it, never exceeding the maximum).
When health reaches zero, the avatar dies. Death has significant consequences:

- Health and mana are **completely restored**.
- The experience accumulated in the current level is **reset** (but the level remains unchanged).
- **100 coins are lost**.

Death is therefore a penalty that does not cause a level regression, but resets recent XP progress and inflicts an economic cost.

## Mana and Spells
**Mana** is the resource needed to cast spells. Like health, it has a current value and a maximum.
It is consumed when a spell is used and can be restored via potions.
The **spells** available in the game are currently three, each with different characteristics:

| Spell | Power | Mana required | Level required |
|---|---|---|---|
| Fireball | 10 | 5 | 5 |
| Blizzard | 15 | 7 | 10 |
| Thunder | 20 | 10 | 15 |

An avatar learns a spell automatically when reaching the required level.
It is not possible to know the same spell twice, nor to cast a spell that is not known.
To cast a spell, sufficient mana is required; otherwise, the operation is rejected.


## Stats
The avatar has three statistics that describe its combat and magical capabilities:

- **Strength:** influences physical attack capability.
- **Defense:** influences damage resistance.
- **Intelligence:** influences the effectiveness of spells.

Each statistic can be manually incremented one point at a time, by spending skill points earned every time a level is gained.
Each increment is notified to the rest of the system via an event (see Events section).


## Inventory and Equipped Items
The avatar has two distinct containers for items:

- **Inventory:** items that the character carries but is not using.
- **Equipped items:** items actively worn or wielded.

The items existing in the domain are of four types:

- **Weapon:** has an attack power value.
- **Armor:** has a defensive power value.
- **Health Potion:** has a healing power value for health.
- **Mana Potion:** has a regenerative power value for mana.

The item management rules are: to **equip** an item, it must be in the inventory.
When equipped, it is removed from the inventory and moved to the equipped items.
When unequipped, it returns to the inventory.
It is not possible to unequip an item that is not equipped or equip an item that is already equipped.
Unequipped items can be sold for money.

## Money
Money is a non-negative numerical resource.
The avatar can earn it (for example by leveling up) or spend it (for example by purchasing items).
It is not possible to spend more money than is owned, nor to reach a negative amount.
As a death penalty, 100 coins are always deducted but the balance cannot drop below zero.
Unequipped items can be sold for money.
If an attempt is made to purchase an item without sufficient money, the operation is rejected.

## Domain Events
Some important actions generate **events** that are propagated to the rest of the system.
The currently defined events are:

- **LevelUpped:** emitted when the avatar levels up. Contains the new level reached.
- **Dead:** emitted when the avatar dies. Signals to the system that the character has run out of health.
- **NewSpellLearned:** emitted when the avatar automatically learns a new spell upon reaching the required level.
- **SkillPointAssigned:** emitted when a stat is incremented (Strength, Defense or Intelligence). Contains the updated stat.

These events are handled by an internal observer that logs them and forwards them to a notification system.
Some events may be signaled to other services in the system, otherwise they are only notified to the user.