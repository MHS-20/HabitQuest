# Avatar API

**Service:** Avatar Service  
**Base Path:** `/api/v1/avatars`

The Avatar Service manages the player character for each user. It handles creation and retrieval of avatars as well as all character state: money, inventory, equipped items, experience, level, health, mana, and stats.

---

## Endpoints

### Create Avatar

**`POST /api/v1/avatars`**

Creates a new avatar.

**Request body:**

```json
{
  "id": "string",
  "name": "string"
}
```

**Response `201 Created`:**

```json
{
  "id": "string",
  "_links": { ... }
}
```

---

### Get Avatar

**`GET /api/v1/avatars/{id}`**

Returns full avatar details.

**Response `200 OK`:**

```json
{
  "id": "string",
  "name": "string",
  "money": { "amount": 100 },
  "level": { "levelNumber": 1, "currentExperience": 0, "experienceRequired": 100 },
  "health": { "current": 100, "max": 100 },
  "mana": { "amount": 50, "max": 50 },
  "stats": { "strength": 10, "defense": 10, "intelligence": 10 },
  "inventory": { "id": "string", "items": [] },
  "equippedItems": { "id": "string", "items": [] },
  "spells": [],
  "_links": { ... }
}
```

---

### Delete Avatar

**`DELETE /api/v1/avatars/{id}`**

Permanently deletes the avatar.

**Response `204 No Content`**

---

### Search Avatars

**`POST /api/v1/avatars/search`**

Searches for avatars by name and/or level range.

**Request body:**

```json
{
  "name": "string (optional)",
  "minLevel": 1,
  "maxLevel": 10
}
```

**Response `200 OK`:** Collection of avatar objects.

---

## Invites

### Get Pending Invites

**`GET /api/v1/avatars/{id}/invites`**

Returns the pending guild invites for the avatar.

**Response `200 OK`:** Collection of invite objects.

### Receive Guild Invite

**`POST /api/v1/avatars/{id}/invites`**

Stores a guild invite for the avatar.

**Request body:**

```json
{
  "inviteId": "string",
  "guildId": "string",
  "guildName": "Guild Name",
  "expiresAt": "2026-04-22T12:00:00Z"
}
```

If `expiresAt` is missing or blank, the service defaults the invite to expire 24 hours from receipt.

**Response `204 No Content`**

### Accept Guild Invite

**`POST /api/v1/avatars/{id}/invites/{inviteId}/accept`**

Accepts a pending guild invite.

**Response `204 No Content`**

---

## Name

### Get Name

**`GET /api/v1/avatars/{id}/name`**

```json
{ "name": "string" }
```

### Update Name

**`PATCH /api/v1/avatars/{id}/name`**

**Request body:**

```json
{ "name": "string" }
```

**Response `204 No Content`**

---

## Money

### Get Money

**`GET /api/v1/avatars/{id}/money`**

```json
{ "amount": 100 }
```

### Earn Money

**`POST /api/v1/avatars/{id}/money/earn`**

**Request body:**

```json
{ "amount": 50 }
```

**Response `204 No Content`**

### Spend Money

**`POST /api/v1/avatars/{id}/money/spend`**

**Request body:**

```json
{ "amount": 25 }
```

**Response `204 No Content`**

---

## Inventory

### Get Inventory

**`GET /api/v1/avatars/{id}/inventory`**

```json
{
  "id": "string",
  "items": [
    { "type": "WEAPON", "name": "Iron Sword", "description": "...", "power": 10 }
  ]
}
```

### Add Item to Inventory

**`POST /api/v1/avatars/{id}/inventory/items`**

**Request body:**

```json
{ "type": "WEAPON", "name": "Iron Sword", "description": "...", "power": 10 }
```

**Response `204 No Content`**

### Remove Item from Inventory

**`DELETE /api/v1/avatars/{id}/inventory/items`**

**Request body:**

```json
{ "type": "WEAPON", "name": "Iron Sword", "description": "...", "power": 10 }
```

**Response `204 No Content`**

### Get Equipped Items

**`GET /api/v1/avatars/{id}/equipped-items`**

Returns the currently equipped items for the avatar.

**Response `200 OK`:**

```json
{
  "id": "string",
  "items": [
    { "type": "WEAPON", "name": "Iron Sword", "description": "...", "power": 10 }
  ]
}
```

### Equip Item

**`POST /api/v1/avatars/{id}/inventory/items/equip`**

**Request body:**

```json
{ "type": "WEAPON", "name": "Iron Sword", "description": "...", "power": 10 }
```

**Response `204 No Content`**

### Unequip Item

**`POST /api/v1/avatars/{id}/inventory/items/unequip`**

**Request body:**

```json
{ "type": "WEAPON", "name": "Iron Sword", "description": "...", "power": 10 }
```

**Response `204 No Content`**

### Equip Item

**`POST /api/v1/avatars/{id}/inventory/items/equip`**

**Request body:**

```json
{ "type": "WEAPON", "name": "Iron Sword", "description": "...", "power": 10 }
```

**Response `204 No Content`**

### Unequip Item

**`POST /api/v1/avatars/{id}/inventory/items/unequip`**

**Request body:**

```json
{ "type": "WEAPON", "name": "Iron Sword", "description": "...", "power": 10 }
```

**Response `204 No Content`**

## Experience & Level

### Get Experience

**`GET /api/v1/avatars/{id}/experience`**

```json
{ "amount": 250 }
```

### Grant Experience

**`POST /api/v1/avatars/{id}/experience/grant`**

**Request body:**

```json
{ "amount": 50 }
```

**Response `204 No Content`**

### Get Level

**`GET /api/v1/avatars/{id}/level`**

```json
{ "levelNumber": 3, "currentExperience": 50, "experienceRequired": 200 }
```

---

## Health

### Get Health

**`GET /api/v1/avatars/{id}/health`**

```json
{ "current": 80, "max": 100 }
```

### Take Damage

**`POST /api/v1/avatars/{id}/health/damage`**

**Request body:**

```json
{ "amount": 20 }
```

**Response `200 OK`:**

```json
{ "died": false }
```

### Heal

**`POST /api/v1/avatars/{id}/health/heal`**

**Request body:**

```json
{ "amount": 20 }
```

**Response `204 No Content`**

---

## Mana

### Get Mana

**`GET /api/v1/avatars/{id}/mana`**

```json
{ "amount": 30, "max": 50 }
```

### Spend Mana

**`POST /api/v1/avatars/{id}/mana/spend`**

**Request body:**

```json
{ "amount": 10 }
```

**Response `204 No Content`**

### Restore Mana

**`POST /api/v1/avatars/{id}/mana/restore`**

**Request body:**

```json
{ "amount": 10 }
```

**Response `204 No Content`**

---

## Stats

### Get Stats

**`GET /api/v1/avatars/{id}/stats`**

```json
{ "strength": 12, "defense": 10, "intelligence": 8 }
```

### Increase Strength

**`POST /api/v1/avatars/{id}/stats/strength`**

**Response `204 No Content`**

### Increase Defense

**`POST /api/v1/avatars/{id}/stats/defense`**

**Response `204 No Content`**

### Increase Intelligence

**`POST /api/v1/avatars/{id}/stats/intelligence`**

**Response `204 No Content`**

---

## Error Responses

| Status | Condition |
|--------|-----------|
| `400 Bad Request` | Invalid request data or business rule violation (e.g. insufficient mana/health) |
| `404 Not Found` | Avatar not found |
