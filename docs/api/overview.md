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

## Service Summary
| Service | Typical create status | Typical mutation status | Response style notes |
|---------|------------------------|-------------------------|----------------------|
| Avatar | `201 Created` | `204 No Content` | Mostly HATEOAS resources; `POST /health/damage` returns `200` with `{ "died": boolean }` |
| Tracking | `201 Created` | `204 No Content` | Habit detail/property routes are HATEOAS; avatar-scoped habits/history routes return plain JSON arrays |
| Quest | `201 Created` | `204 No Content` | HATEOAS resources for quest endpoints, including progress wrapper resource |
| Guild/Battle | `201 Created` | `204 No Content` | HATEOAS resources for guild and battle queries |
| Marketplace | `201 Created` | `204 No Content` | HATEOAS resources for marketplace and item queries |

### Authentication
All endpoints are protected and require a JWT token, except for the authentication endpoints.
The `/auth/login` and `/auth/register` endpoints authenticates users and issues JWT tokens for subsequent requests.
Registration triggers the creation of a new avatar with default attributes and dedicated marketplace for the user.
The login endpoint validates user credentials and returns a JWT token that encodes the user's identity.

The required `Authorization` header format for protected endpoints is:
```
Authorization: Bearer <token>
```

### Content Type 
All request and response bodies use JSON:
```
Content-Type: application/json
```

### Error Responses
Common status codes across services:
| HTTP Status | Condition |
|-------------|-----------|
| `400 Bad Request` | Invalid input or business rule violation |
| `403 Forbidden` | Caller lacks permission for the operation |
| `404 Not Found` | Requested resource does not exist |
| `502 Bad Gateway` | Downstream service communication failure (for example quest/marketplace integrations) |

Most `400` and `502` responses include a JSON body with a `message` field; some `404` paths return an empty body depending on service.

**Typical error response body:**

```json
{
  "message": "Description of the error"
}
```
