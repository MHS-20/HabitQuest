## Domain: Battles

A **Battle** is a cooperative event in which the members of a guild face a **Boss** together.
It is a turn-based activity where players take turns attacking the boss and trying to defeat it before all members fall in battle.

### The Enemy Boss
The boss is the enemy that the guild must defeat.

Each Boss has the following characteristics:

- **Name**: boss identifier (e.g. "Minotaur")
- **Combat stats**:
    - **Health**: total hit points of the boss (e.g. 100 HP)
    - **Strength**: offensive capability (e.g. 150)
    - **Defense**: defensive capability (e.g. 50)

Each Boss entails Rewards and Penalties:

- **Money Reward**: coins the guild obtains by defeating the boss (e.g. 100 coins)
- **Experience Reward**: experience points assigned individually (e.g. 200 XP)
- **Penalty**: damage/loss suffered if the guild loses (e.g. -50 coins)

Each guild can have only one active battle at a time.
Only the leader can decide to start a battle by choosing a boss to fight.
Bosses are statically predefined in the system, but it easily supports the addition of new boss types.

### Battle Mechanics

#### Turn-Based System
The battle works with a **rotating turn system**:

1. When the battle starts, the **number of turns** is defined based on the participating members
2. There is always a **current turn** that identifies which member can act
3. After each action, the turn passes to the next member (circular rotation)
4. If a member has fallen, they are automatically skipped in the rotation

#### Battle States
A battle can be in three states:

1. **Ongoing**
    - The battle is active
    - Members can perform actions
    - Neither the boss nor all members have been defeated

2. **Won**
    - The boss has been reduced to 0 HP
    - The guild receives experience and money rewards
    - The battle ends

3. **Lost**
    - All guild members have fallen
    - The guild suffers the boss penalty
    - The battle ends

#### Damage Mechanics
**Damage to the Boss:**

- During their turn, a member can deal damage to the boss
- Damage reduces the boss's remaining health
- When health reaches 0, the battle is won

**Boss Counterattack:**

- The boss automatically counterattacks whoever attacks it
- Counterattack damage can cause the guild member to fall
- Fallen members can no longer participate in the battle

#### Fallen Members
When a member falls:

- They are added to the list of **fallen members**
- They can no longer perform actions
- They are skipped in the turn rotation
- If **all** members fall, the battle is lost

### Dynamic Participation
The battle supports dynamic joining and leaving of members:

Increase in Participants:

- A new member can join an ongoing battle
- The number of turns increases
- The new member is added to the rotation

Reduction in Participants:

- A member can leave the battle
- The number of turns decreases
- The member is removed from the rotation and from the fallen list

### Battle Events
The possible events related to the battle are:

- **BattleStarted**: the battle has started
- **AttackPerformed**: a member has attacked
- **SpellCasted**: a spell has been cast
- **BattleWon**: the battle has been won
- **BattleLost**: the battle has been lost

### Battle Invariants
1. **One Battle per Guild**: each guild can have at most one active battle
2. **Non-Negative Health**: the boss's health cannot drop below zero (it stops at 0)
3. **Valid Turn**: the current turn must always point to an existing and non-fallen member
4. **Minimum Turns**: there must be at least 1 turn (at least one participant)
5. **Victory Condition**: battle won if and only if boss health = 0
6. **Defeat Condition**: battle lost if and only if all members have fallen
7. **Boss Immutability**: the boss's characteristics (except remaining health) do not change during the battle

### Valid State Transitions
**For Battles:**

- Created → Ongoing (automatic)
- Ongoing → Won (boss defeated)
- Ongoing → Lost (all fallen)
- Won/Lost → Cancelled