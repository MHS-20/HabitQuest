# API Overview

HabitQuest exposes a RESTful HTTP API distributed across multiple microservices. All services are fronted by the **Edge Service**, which handles authentication and routes requests to the appropriate backend service.

## Base URLs

| Service | Base Path |
|---------|-----------|
| Authentication (Edge Service) | `/auth` |
| Avatar Service | `/api/v1/avatars` |
| Tracking Service (Habits) | `/api/v1/habits` |
| Quest Service | `/api/v1/quests` |
| Guild Service | `/api/v1/guilds` |
| Battle Service | `/api/v1/battles` |
| Marketplace Service | `/api/v1/marketplaces` |

## Authentication

Protected endpoints require a Bearer token obtained from the `/auth/login` or `/auth/register` endpoints.

```
Authorization: Bearer <token>
```

## Content Type

All request and response bodies use JSON:

```
Content-Type: application/json
```

## HATEOAS

Responses are wrapped in Spring HATEOAS `EntityModel` or `CollectionModel` objects, which include `_links` fields with hypermedia navigation links to related resources.

## Error Responses

All services follow a consistent error format:

| HTTP Status | Condition |
|-------------|-----------|
| `400 Bad Request` | Invalid input or business rule violation |
| `403 Forbidden` | Caller lacks permission for the operation |
| `404 Not Found` | Requested resource does not exist |
| `502 Bad Gateway` | Downstream service communication failure |

**Error response body:**

```json
{
  "message": "Description of the error"
}
```
