# Authentication API

**Service:** Edge Service  
**Base Path:** `/auth`

The Edge Service is the entry point for all client traffic. It handles user registration and login, issuing JWT tokens used to authenticate subsequent requests.

---

## Endpoints

### Register

**`POST /auth/register`**

Creates a new user account and automatically provisions an avatar for the user.

**Request body:**

```json
{
  "name": "string (required)",
  "email": "user@example.com (required, valid email)",
  "password": "string (required, min 8 characters)"
}
```

**Response `200 OK`:**

```json
{
  "token": "eyJhbGci...",
  "userId": "uuid"
}
```

---

### Login

**`POST /auth/login`**

Authenticates an existing user and returns a JWT token.

**Request body:**

```json
{
  "email": "user@example.com (required, valid email)",
  "password": "string (required)"
}
```

**Response `200 OK`:**

```json
{
  "token": "eyJhbGci...",
  "userId": "uuid"
}
```

---

### Validate Token

**`POST /auth/validate`**

Checks whether a JWT token is valid. Used internally by the gateway to authenticate downstream requests.

**Request header:**

```
Authorization: Bearer <token>
```

**Response `200 OK`:**

```json
{
  "valid": true
}
```

---

## Error Responses

| Status | Condition |
|--------|-----------|
| `400 Bad Request` | Missing or invalid fields (e.g. email format, password too short) |
| `401 Unauthorized` | Invalid credentials during login |
