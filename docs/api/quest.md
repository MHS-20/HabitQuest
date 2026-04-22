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
  "name": "string",
  "durationDays": 30
}
```

**Response `201 Created`:**

```json
{
  "id": "string",
  "_links": {
    "self": { "href": "/api/v1/quests/string" }
  }
}
```

---

### Get All Quests

**`GET /api/v1/quests`**

Returns the list of quests in the system.

**Response `200 OK`:** HATEOAS collection of quest objects.

```json
[
  {
    "id": "string",
    "name": "string",
    "durationDays": 30,
    "reward": 100,
    "habitIds": ["habit-id-1", "habit-id-2"],
    "_links": {
      "self": { "href": "/api/v1/quests/string" }
    }
  }
]
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
  "durationDays": 30,
  "reward": 100,
  "habitIds": ["habit-id-1", "habit-id-2"],
  "_links": {
    "self": { "href": "/api/v1/quests/string" }
  }
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

Returns the quest duration in days.

```json
{ "durationDays": 30 }
```

### Update Duration

**`PATCH /api/v1/quests/{id}/duration`**

**Request body:**

```json
{ "durationDays": 14 }
```

**Response `204 No Content`**

---

### Get Reward

**`GET /api/v1/quests/{id}/reward`**

```json
{ "value": 100 }
```

### Update Reward

**`PATCH /api/v1/quests/{id}/reward`**

**Request body:**

```json
{ "experience": 750, "money": 200 }
```

> The current command payload still accepts `experience`, but the controller maps the update to the quest's money reward value.

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
      "recurrence": { "type": "DAILY", "dayOfMonth": null, "dayOfWeek": null },
      "nextRecurrenceDate": "2026-04-03",
      "lastAttendedDate": "2026-04-02"
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

**Response `200 OK`:** HATEOAS resource containing the avatar progress summary.

```json
{
  "avatarId": "string",
  "quests": [
    {
      "questId": "string",
      "questName": "Morning Routine",
      "status": "IN_PROGRESS",
      "completionPercentage": 50,
      "habits": [
        {
          "habitId": "string",
          "title": "Morning Run",
          "requiredOccurrences": 2,
          "attendedOccurrences": 1,
          "remainingOccurrences": 1
        }
      ]
    }
  ]
}
```

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
