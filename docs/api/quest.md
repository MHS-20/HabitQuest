# Quest API

**Service:** Quest Service  
**Base Path:** `/api/v1/quests`

The Quest Service manages quests — structured challenges that group one or more habits under a shared goal, duration, and reward. Quests can be associated with guilds or individual avatars.

---

## Endpoints

### Create Quest

**`POST /api/v1/quests`**

Creates a new quest.

**Request body:**

```json
{
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

### Get Quest

**`GET /api/v1/quests/{id}`**

Returns full quest details.

**Response `200 OK`:**

```json
{
  "id": "string",
  "name": "string",
  "duration": "P30D",
  "reward": { "experience": 500, "money": 100 },
  "habitIds": ["habit-id-1", "habit-id-2"],
  "_links": { ... }
}
```

---

### Delete Quest

**`DELETE /api/v1/quests/{id}`**

Permanently deletes the quest.

**Response `204 No Content`**

---

## Quest Properties

### Get Name

**`GET /api/v1/quests/{id}/name`**

```json
{ "name": "30-Day Running Challenge" }
```

### Update Name

**`PATCH /api/v1/quests/{id}/name`**

**Request body:**

```json
{ "name": "30-Day Running Challenge" }
```

**Response `204 No Content`**

---

### Get Duration

**`GET /api/v1/quests/{id}/duration`**

Returns the quest duration as an ISO 8601 duration string.

```json
{ "duration": "P30D" }
```

### Update Duration

**`PATCH /api/v1/quests/{id}/duration`**

**Request body:**

```json
{ "duration": "P14D" }
```

**Response `204 No Content`**

---

### Get Reward

**`GET /api/v1/quests/{id}/reward`**

```json
{ "experience": 500, "money": 100 }
```

### Update Reward

**`PATCH /api/v1/quests/{id}/reward`**

**Request body:**

```json
{ "experience": 750, "money": 200 }
```

**Response `204 No Content`**

---

## Habits

### Get Quest Habits

**`GET /api/v1/quests/{id}/habits`**

Returns the habits associated with this quest.

```json
{
  "habits": [
    {
      "id": "string",
      "title": "Morning Run",
      "description": "Run 5km",
      "tags": ["fitness"],
      "recurrence": { "type": "DAILY", "dayOfMonth": null, "dayOfWeek": null }
    }
  ]
}
```

### Add Habit to Quest

**`POST /api/v1/quests/{id}/habits`**

Adds a habit (identified by `habitId`) to this quest, along with its metadata. The habit details are stored within the quest context and are used when replicating the habit to a participant's personal plan.

**Request body:**

```json
{
  "habitId": "string",
  "title": "Morning Run",
  "description": "Run 5km",
  "tags": ["fitness"],
  "recurrence": {
    "type": "DAILY",
    "dayOfMonth": null,
    "dayOfWeek": null
  }
}
```

**Response `204 No Content`**

### Get Active Quest Progress

**`GET /api/v1/quests/progress/{avatarId}`**

Returns the active quest progress for an avatar.

**Response `200 OK`:** Collection of quest progress entries.

### Remove Habit from Quest

**`DELETE /api/v1/quests/{id}/habits`**

Removes a habit from this quest. Both `habitId` and `title` are required as they together identify the habit entry within the quest.

**Request body:**

```json
{
  "habitId": "string",
  "title": "Morning Run"
}
```

**Response `204 No Content`**

---

## Error Responses

| Status | Condition |
|--------|-----------|
| `400 Bad Request` | Invalid duration format or business rule violation |
| `404 Not Found` | Quest not found |
