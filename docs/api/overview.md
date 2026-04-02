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
