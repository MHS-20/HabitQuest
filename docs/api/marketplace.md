# Marketplace API

**Service:** Marketplace Service  
**Base Path:** `/api/v1/marketplaces`

The Marketplace Service provides each avatar with a personal shop. Avatars can browse available items filtered by type, purchase them using in-game currency, and sell owned items back. Buy and sell operations use a saga pattern to maintain consistency between the avatar's inventory and wallet.

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

**Response `200 OK`:**

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
      "price": 100
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

**Response `200 OK`:** Collection of item objects.

```json
[
  {
    "type": "ARMOR",
    "name": "Leather Armor",
    "description": "Basic leather armor.",
    "power": 5,
    "price": 50
  }
]
```

---

### Get Item by Name

**`GET /api/v1/marketplaces/{marketplaceId}/items/{itemName}`**

Returns a single available item by name.

**Response `200 OK`:**

```json
{
  "type": "WEAPON",
  "name": "Iron Sword",
  "description": "A sturdy iron sword.",
  "power": 15,
  "price": 100
}
```

---

## Sold Items

### Get Sold Items

**`GET /api/v1/marketplaces/{marketplaceId}/sold-items`**

Returns items previously sold by the avatar (their inventory items listed for resale).

**Response `200 OK`:** Collection of item objects.

---

### Get Sold Item by Name

**`GET /api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}`**

Returns a single sold item by name.

**Response `200 OK`:** Item object.

---

## Commerce

### Buy Item

**`POST /api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy`**

Purchases an item from the marketplace. Deducts the price from the avatar's wallet and adds the item to the inventory. Uses a saga pattern for transactional consistency.

**Query parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `currentLevel` | `Integer` | The avatar's current level (used to check purchase eligibility) |

**Response `204 No Content`**

---

### Sell Item

**`POST /api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}/sell`**

Sells an item from the avatar's inventory. Removes the item from inventory and credits the avatar's wallet. Uses a saga pattern for transactional consistency.

**Response `204 No Content`**

---

## Error Responses

| Status | Condition |
|--------|-----------|
| `400 Bad Request` | Insufficient funds, item not found in inventory, or other business rule violation |
| `404 Not Found` | Marketplace or item not found |
| `502 Bad Gateway` | Avatar Service communication failure during saga execution |
