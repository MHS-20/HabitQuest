# Guild & Battle API

**Service:** Guild Service  
**Base Paths:** `/api/v1/guilds`, `/api/v1/battles`

The Guild Service manages player guilds and cooperative boss battles. Guilds have role-based member management (leader, officer, member). Battles allow guild members to collectively fight boss enemies, with damage distributed across turns.

---

## Guild Endpoints

### Create Guild

**`POST /api/v1/guilds`**

Creates a new guild. The creator is automatically assigned the **Leader** role.

**Request body:**

```json
{
  "name": "string",
  "creatorAvatarId": "string",
  "creatorNickname": "string"
}
```

**Response `201 Created`:**

```json
{
  "id": "string",
  "_links": {
    "self": { "href": "/api/v1/guilds/string" }
  }
}
```

---

### Get Guild

**`GET /api/v1/guilds/{id}`**

Returns full guild details including all members.

**Response `200 OK`:**

```json
{
  "id": "string",
  "name": "string",
  "globalRank": 5,
  "members": [
    { "avatarId": "string", "nickname": "string", "role": "LEADER" }
  ],
  "_links": {
    "self": { "href": "/api/v1/guilds/string" }
  }
}
```

---

### Delete Guild

**`DELETE /api/v1/guilds/{id}`**

Permanently deletes the guild. Only the guild leader can perform this action.

**Response `204 No Content`**

---

## Member Management

### Get Members

**`GET /api/v1/guilds/{id}/members`**

Returns all guild members.

**Response `200 OK`:** HATEOAS collection of member objects.

```json
[
  { "avatarId": "string", "nickname": "string", "role": "MEMBER" }
]
```

---

### Send Invite

**`POST /api/v1/guilds/{id}/invites`**

Invites another avatar to join the guild. Only leaders and officers can send invites.

**Request body:**

```json
{
  "requestorId": "string",
  "targetAvatarId": "string"
}
```

**Response `204 No Content`**

---

### Accept Invite

**`POST /api/v1/guilds/{id}/invites/{inviteId}/accept`**

Accepts a pending guild invite. The invited avatar is added to the guild.

**Request body:**

```json
{
  "avatarId": "string",
  "nickname": "string"
}
```

**Response `204 No Content`**

---

### Remove Member

**`DELETE /api/v1/guilds/{id}/members/{memberId}`**

Removes a member from the guild. Only the leader can remove members.

**Request body:**

```json
{ "requestorId": "string" }
```

**Response `204 No Content`**

---

### Leave Guild

**`POST /api/v1/guilds/{id}/members/{memberId}/leave`**

The specified member voluntarily leaves the guild.

**Response `204 No Content`**

---

### Change Member Role

**`PATCH /api/v1/guilds/{id}/members/{memberId}/role`**

Promotes or changes the role of a guild member. Only the leader can change roles.

**Request body:**

```json
{
  "roleName": "OFFICER | MEMBER",
  "requestorId": "string"
}
```

**Response `204 No Content`**

---

## Leaderboard

### Get Guild Rank

**`GET /api/v1/guilds/{id}/rank`**

```json
{ "globalRank": 5 }
```

### Get Leaderboard

**`GET /api/v1/guilds/leaderboard`**

Returns all guilds ordered by global rank.

**Response `200 OK`:** Collection of guild objects.

---

## Battle Endpoints

### Create Battle

**`POST /api/v1/battles`**

Creates a new boss battle for a guild. Only the guild leader can initiate a battle.

**Request body:**

```json
{
  "guildId": "string",
  "bossType": "string",
  "requesterId": "string"
}
```

**Response `201 Created`:**

```json
{
  "id": "string",
  "_links": {
    "self": { "href": "/api/v1/battles/string" }
  }
}
```

---

### Get Battle

**`GET /api/v1/battles/{id}`**

Returns full battle details.

**Response `200 OK`:**

```json
{
  "id": "string",
  "guildId": "string",
  "status": "IN_PROGRESS | WON | LOST",
  "currentTurn": 2,
  "numOfTurns": 10,
  "boss": {
    "name": "Dragon",
    "health": 1000,
    "strength": 50,
    "defense": 20,
    "experienceReward": 500,
    "moneyReward": 200,
    "penalty": 100
  },
  "bossRemainingHealth": 750,
  "_links": {
    "self": { "href": "/api/v1/battles/string" }
  }
}
```

---

### Delete Battle

**`DELETE /api/v1/battles/{id}`**

Cancels and deletes the battle. Only the guild leader can perform this action.

**Request body:**

```json
{
  "guildId": "string",
  "requesterId": "string"
}
```

**Response `204 No Content`**

---

### Get Guild's Active Battle

**`GET /api/v1/battles/guild/{guildId}`**

Returns the active battle for the specified guild.

**Response `200 OK`:** Battle object (same schema as Get Battle).

---

### Get All Boss Types

**`GET /api/v1/battles/boss`**

Returns the catalog of available boss types.

**Response `200 OK`:** HATEOAS collection of boss objects.

```json
[
  {
    "name": "Dragon",
    "health": 1000,
    "strength": 50,
    "defense": 20,
    "experienceReward": 500,
    "moneyReward": 200,
    "penalty": 100
  }
]
```

---

### Check Battle In Progress

**`GET /api/v1/battles/guild/{guildId}/in-progress`**

```json
{ "inProgress": true }
```

---

## Battle State

### Get Boss

**`GET /api/v1/battles/{id}/boss`**

```json
{
  "name": "Dragon",
  "health": 1000,
  "strength": 50,
  "defense": 20,
  "experienceReward": 500,
  "moneyReward": 200,
  "penalty": 100
}
```

### Get Boss Health

**`GET /api/v1/battles/{id}/boss/health`**

```json
{ "remainingHealth": 750 }
```

### Get Current Turn

**`GET /api/v1/battles/{id}/turns/current`**

```json
{ "turn": 3 }
```

### Get Total Turns

**`GET /api/v1/battles/{id}/turns/total`**

```json
{ "turn": 10 }
```

### Get Battle Status

**`GET /api/v1/battles/{id}/status`**

```json
{
  "status": "IN_PROGRESS",
  "isOver": false,
  "isWon": false
}
```

---

## Combat

### Deal Damage

**`POST /api/v1/battles/{id}/damage`**

Records an attack by a guild member against the boss.

**Request body:**

```json
{
  "damage": 75,
  "attackerAvatarId": "string"
}
```

**Response `204 No Content`**

---

## Error Responses

| Status | Condition |
|--------|-----------|
| `400 Bad Request` | Invalid request data or business rule violation |
| `403 Forbidden` | Caller does not have the required guild role (e.g. non-leader attempts leader-only action) |
| `404 Not Found` | Guild or battle not found |
