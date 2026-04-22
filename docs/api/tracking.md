# Tracking API (Habits)

**Service:** Tracking Service  
**Base Path:** `/api/v1/habits`

The Tracking Service manages the lifecycle of habits. A habit belongs to an avatar and has a recurrence schedule (daily, weekly, or monthly). Attending a habit records a completion event and triggers gamification rewards via domain events.

---

## Endpoints

### Create Habit

**`POST /api/v1/habits`**

Creates a new habit for an avatar.

**Request body:**

```json
{
  "avatarId": "string",
  "title": "string",
  "description": "string",
  "recurrenceType": "DAILY | WEEKLY | MONTHLY",
  "dayOfWeek": "MONDAY",
  "dayOfMonth": 1,
  "tags": ["health", "exercise"],
  "associatedQuestId": "string",
  "sourceHabitId": "string"
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `avatarId` | Yes | |
| `title` | Yes | |
| `description` | No | |
| `recurrenceType` | Yes | `DAILY`, `WEEKLY`, or `MONTHLY` |
| `dayOfWeek` | Conditional | Required when `recurrenceType` is `WEEKLY` |
| `dayOfMonth` | Conditional | Required when `recurrenceType` is `MONTHLY` |
| `tags` | No | Optional list of tags attached to the habit at creation time |
| `associatedQuestId` | No | Links the habit to a quest when the habit originates from quest replication |
| `sourceHabitId` | No | Identifies the upstream habit when this habit is cloned from another source |

**Response `201 Created`:**

```json
{
  "id": "string",
  "_links": {
    "self": { "href": "/api/v1/habits/string" }
  }
}
```

---

### Get Habit

**`GET /api/v1/habits/{id}`**

Returns full habit details.

**Response `200 OK`:**

```json
{
  "id": "string",
  "avatarId": "string",
  "title": "string",
  "description": "string",
  "tags": ["health", "exercise"],
  "recurrence": {
    "type": "WEEKLY",
    "dayOfMonth": null,
    "dayOfWeek": "MONDAY"
  },
  "lastAttendedDate": "2024-01-15T08:00:00",
  "nextRecurrenceDate": "2024-01-22T08:00:00",
  "associatedQuestId": "string | null",
  "sourceHabitId": "string | null",
  "_links": {
    "self": { "href": "/api/v1/habits/string" }
  }
}
```

---

### Delete Habit

**`DELETE /api/v1/habits/{id}`**

Permanently deletes the habit.

**Response `204 No Content`**

---

## Habit Properties

### Get Title

**`GET /api/v1/habits/{id}/title`**

```json
{ "title": "Morning Run" }
```

### Update Title

**`PATCH /api/v1/habits/{id}/title`**

**Request body:**

```json
{ "title": "Morning Run" }
```

**Response `204 No Content`**

---

### Get Description

**`GET /api/v1/habits/{id}/description`**

```json
{ "description": "Run 5km every morning." }
```

### Update Description

**`PATCH /api/v1/habits/{id}/description`**

**Request body:**

```json
{ "description": "Run 5km every morning." }
```

**Response `204 No Content`**

---

### Get Tags

**`GET /api/v1/habits/{id}/tags`**

```json
{ "tags": ["health", "fitness"] }
```

### Update Tags

**`PATCH /api/v1/habits/{id}/tags`**

**Request body:**

```json
{ "tags": ["health", "fitness", "outdoor"] }
```

**Response `204 No Content`**

---

### Get Recurrence

**`GET /api/v1/habits/{id}/recurrence`**

```json
{
  "type": "WEEKLY",
  "dayOfMonth": null,
  "dayOfWeek": "MONDAY"
}
```

### Update Recurrence

**`PATCH /api/v1/habits/{id}/recurrence`**

**Request body:**

```json
{
  "type": "MONTHLY",
  "dayOfWeek": null,
  "dayOfMonth": 15
}
```

**Response `204 No Content`**

---

### Get Last Attended Date

**`GET /api/v1/habits/{id}/last-attended-date`**

```json
{ "date": "2024-01-15T08:00:00" }
```

---

### Get History

**`GET /api/v1/habits/{id}/history`**

Returns the full completion history of the habit.

```json
{
  "history": [
    {
      "eventType": "HabitAttended",
      "habitId": "string",
      "avatarId": "string",
      "occurredAt": "2024-01-08T08:00:00",
      "details": "Completed a morning run"
    }
  ]
}
```

### Get Habits by Avatar

**`GET /api/v1/habits/avatar/{avatarId}`**

Returns the active habits belonging to the avatar.

**Response `200 OK`:** Plain JSON array of habit objects.

```json
[
  {
    "id": "string",
    "avatarId": "string",
    "title": "Morning Run",
    "description": "Run 5km every morning.",
    "tags": ["health", "fitness"],
    "recurrence": {
      "type": "WEEKLY",
      "dayOfMonth": null,
      "dayOfWeek": "MONDAY"
    },
    "lastAttendedDate": "2024-01-15T08:00:00",
    "nextRecurrenceDate": "2024-01-22T08:00:00",
    "associatedQuestId": "string | null",
    "sourceHabitId": "string | null"
  }
]
```

### Get History by Avatar

**`GET /api/v1/habits/avatar/{avatarId}/history`**

Returns the aggregated habit history for the avatar across all of their habits.

**Response `200 OK`:** Plain JSON array of habit history events.

```json
[
  {
    "eventType": "HabitAttended",
    "habitId": "string",
    "avatarId": "string",
    "occurredAt": "2024-01-08T08:00:00",
    "details": "Completed a morning run"
  }
]
```

---

## Attending a Habit

### Mark Habit as Attended

**`POST /api/v1/habits/{id}/attend`**

Records a habit completion event. This triggers gamification domain events (XP reward, etc.).

**Request body:**

```json
{ "date": "2024-01-15T08:00:00" }
```

**Response `204 No Content`**

---

## Error Responses

| Status | Condition |
|--------|-----------|
| `400 Bad Request` | Invalid recurrence configuration or business rule violation |
| `502 Bad Gateway` | Downstream quest synchronization failed while processing habit attendance |
| `404 Not Found` | Habit not found |
