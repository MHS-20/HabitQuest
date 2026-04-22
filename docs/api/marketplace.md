# Marketplace API

**Service:** Marketplace Service  
**Base Path:** `/api/v1/marketplaces`

The Marketplace Service provides each avatar with a personal shop. Avatars can browse available items filtered by type, purchase them using in-game currency, and sell owned items back. Buy and sell operations use a saga pattern to maintain consistency between the avatar's inventory and wallet.

Responses are exposed as HATEOAS resources. The top-level marketplace response is an `EntityModel`, and item listings are returned as `CollectionModel<EntityModel<ItemResponse>>`.

---

## Endpoints

### Create Marketplace

**`POST /api/v1/marketplaces`**

Provisions a new marketplace for an avatar.

**Request body:**

```json
{
  "avatarId": "string"
}
```

**Response `201 Created`:**

```json
{
  "id": "string",
  "items": [],
  "_links": { ... }
}
```

---

### Get Marketplace

**`GET /api/v1/marketplaces/{marketplaceId}`**

Returns marketplace details and its available items.

`GET /api/v1/marketplaces/by-avatar/{avatarId}` returns the same canonical marketplace resource for the avatar.

**Response `200 OK`:**

```json
{
  "id": "string",
  "items": [
    {
      "type": "WEAPON",
      "name": "Iron Sword",
      "description": "A sturdy iron sword.",
      "power": 15,
      "price": 100,
      "requiredLevel": 5
    }
  ],
  "_links": { ... }
}
```

### Get Marketplace by Avatar

**`GET /api/v1/marketplaces/by-avatar/{avatarId}`**

Returns the marketplace owned by the avatar.


**Response `200 OK`:**

```json
{
  "id": "string",
  "items": [
    {
      "type": "WEAPON",
      "name": "Iron Sword",
      "description": "A sturdy iron sword.",
      "power": 15,
      "price": 100,
      "requiredLevel": 5
    }
  ],
  "_links": { ... }
}
```

---

## Items

### Get Available Items

**`GET /api/v1/marketplaces/{marketplaceId}/items`**

Returns items available for purchase, optionally filtered by type.

**Query parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `type` | `ItemType` | `ALL` | Filter by item type (e.g. `WEAPON`, `ARMOR`, `CONSUMABLE`) |

**Response `200 OK`:** HATEOAS collection of available items.

```json
[
  {
    "type": "ARMOR",
    "name": "Leather Armor",
    "description": "Basic leather armor.",
    "power": 5,
    "price": 50,
    "requiredLevel": 1
  }
]
```

---

### Get Item by Name

**`GET /api/v1/marketplaces/{marketplaceId}/items/{itemName}`**

Returns a single available item by name.

> The current implementation expects an `ItemCommand` request body even for this lookup endpoint, because the controller resolves the item through the same mapper used for commerce commands. The `itemName` path variable is present, but the body drives the lookup today.

**Request body:**

```json
{
  "type": "WEAPON",
  "itemName": "Iron Sword",
  "description": "A sturdy iron sword.",
  "power": 15,
  "price": 100,
  "requiredLevel": 5
}
```

**Response `200 OK`:** HATEOAS item resource.

```json
{
  "type": "WEAPON",
  "name": "Iron Sword",
  "description": "A sturdy iron sword.",
  "power": 15,
  "price": 100,
  "requiredLevel": 5
}
```

---

## Sold Items

### Get Sold Items

**`GET /api/v1/marketplaces/{marketplaceId}/sold-items`**

Returns items previously sold by the avatar (their inventory items listed for resale).

**Response `200 OK`:** HATEOAS collection of sold items.

---

### Get Sold Item by Name

**`GET /api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}`**

Returns a single sold item by name.

> As with available items, the current controller expects an `ItemCommand` body for this lookup. The path variable is present, but the request body is what the controller converts into the item lookup value.

**Request body:**

```json
{
  "type": "WEAPON",
  "itemName": "Iron Sword",
  "description": "A sturdy iron sword.",
  "power": 15,
  "price": 100,
  "requiredLevel": 5
}
```

**Response `200 OK`:** HATEOAS item resource.

---

## Commerce

### Buy Item

**`POST /api/v1/marketplaces/{marketplaceId}/items/buy`**

Purchases an item from the marketplace. Deducts the price from the avatar's wallet and adds the item to the inventory. Uses a saga pattern for transactional consistency.

**Query parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `currentLevel` | `Integer` | The avatar's current level (used to check purchase eligibility) |

**Request body:**

```json
{
  "type": "WEAPON",
  "itemName": "Iron Sword",
  "description": "A sturdy iron sword.",
  "power": 15,
  "price": 100,
  "requiredLevel": 5
}
```

**Response `204 No Content`**

Errors for this endpoint map to:

- `403 Forbidden` when the avatar level is below the item requirement
- `404 Not Found` when the marketplace or item cannot be found
- `502 Bad Gateway` when the avatar service cannot be reached

---

### Sell Item

**`POST /api/v1/marketplaces/{marketplaceId}/sold-items/sell`**

Sells an item from the avatar's inventory. Removes the item from inventory and credits the avatar's wallet. Uses a saga pattern for transactional consistency.

**Request body:**

```json
{
  "type": "WEAPON",
  "itemName": "Iron Sword",
  "description": "A sturdy iron sword.",
  "power": 15,
  "price": 100,
  "requiredLevel": 5
}
```

**Response `204 No Content`**

Errors for this endpoint map to:

- `404 Not Found` when the marketplace or item cannot be found
- `502 Bad Gateway` when the avatar service cannot be reached

---

## Error Responses

| Status | Condition |
|--------|-----------|
| `403 Forbidden` | Avatar level is below the item requirement (`InsufficientLevelException`) |
| `404 Not Found` | Marketplace or item not found |
| `502 Bad Gateway` | Avatar Service communication failure during saga execution |
| `400 Bad Request` | Malformed request body or invalid query parameter values |
