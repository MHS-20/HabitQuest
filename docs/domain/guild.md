# Domain: Guilds

A **Guild** is a group of players who come together to collaborate and face common challenges.
It is a social entity that allows players to:

- Join forces to fight boss enemies
- Compete with other guilds via a global leaderboard
- Organize themselves through roles and hierarchies
- Invite new members and manage membership

Each guild member is identified by:

- A unique **Avatar ID** that identifies them in the system
- A **Nickname** visible to other members
- A **Role** within the guild

### Organizational Structure
Each guild has a hierarchical structure with three roles:

1. **Leader** (Guild Master)
   - The one who created the guild
   - Has full control over the guild
   - Can invite new members
   - Can remove members
   - Can promote members to other roles

2. **Officer**
   - Intermediate role of trust
   - Obtained through promotion by the Leader
   - Has no particular administrative powers

3. **Member**
   - Base role of all new members
   - Can participate in battles
   - Can leave the guild voluntarily

### Invite System
The process of joining a guild works through an invite system:

1. Only the **Leader** can send invites
2. An invite is composed of:
   - Unique invite ID
   - ID of the inviting guild
   - ID of the avatar to invite
   - Invite expiration date

3. Invites remain **pending** until:
   - The invited player accepts
   - They expire (via the expiration date)
   - They are rejected

4. When an invite is accepted:
   - The invite is removed from the pending invites list
   - The player becomes a guild member with the **Member** role

### Valid State Transitions
**For Guilds**:

- Created → Active (with members)
- Active → With ongoing battle
- Active → Cancelled

### Guild Invariants
1. **Leader Uniqueness**: each guild has exactly one Leader at the time of creation
2. **Authorization**: only the Leader can:
   - Send invites
   - Remove members
   - Promote members
3. **Duplicate Prevention**: it is not possible to invite an avatar who is already a guild member
4. **Invite Validity**: an invite can only be accepted by the correct recipient
5. **ID Immutability**: the guild ID cannot be modified after creation