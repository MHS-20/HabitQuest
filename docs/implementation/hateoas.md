# HATEOAS in the HabitQuest API
In Spring, the HATEOAS (Hypermedia As The Engine Of Application State) principle is implemented through the **Spring HATEOAS** library, which provides `EntityModel` (single resource with links) and `CollectionModel` (collection of resources with links), built via `WebMvcLinkBuilder`.
This way the client does not need to know the URL structure in advance, but discovers it dynamically from the response itself.
All REST Controllers use HATEOAS to make the API navigable and self-describing; below, only the AvatarController is shown as an example.

As a result, the client can potentially contain no hardcoded URLs.
If the server moves or renames an endpoint, it is sufficient to update the links in the responses: clients that correctly navigate the hypermedia adapt without any modifications.
This is particularly useful in a microservices architecture where endpoints can evolve independently.

A client can navigate the entire API starting from a single entry point (`/avatars/{id}`), progressively discovering resources and operations.
Links are not just passive navigation: they also indicate which actions are available at any given moment. A `health` resource that exposes `heal` and `damage` implicitly communicates that these are the legal operations on it.

Building links via `WebMvcLinkBuilder` and `methodOn()` ensures that the URLs in the links are always aligned with the controller's `@RequestMapping` annotations.
There is no risk of broken links due to refactoring: if the path of a method changes, the link updates automatically.

### Avatar creation (`POST /api/v1/avatars`)
At creation, the response immediately includes links to the main child resources, so the client knows what it can do right after:
```java
EntityModel.of(
    body,
    selfLink(id.value()),
    linkTo(methodOn(AvatarController.class).getAvatar(id.value())).withRel("avatar"),
    linkTo(methodOn(AvatarController.class).getLevel(id.value())).withRel("level"),
    linkTo(methodOn(AvatarController.class).getHealth(id.value())).withRel("health")
);
```

### Avatar detail (`GET /api/v1/avatars/{id}`)
This is the main entry point and returns the richest set of links: all resources navigable from the avatar.
```java
EntityModel.of(
    dto,
    selfLink(id),
    linkTo(...getInventory(id)).withRel("inventory"),
    linkTo(...getEquippedItems(id)).withRel("equippedItems"),
    linkTo(...getStats(id)).withRel("stats"),
    linkTo(...getLevel(id)).withRel("level"),
    linkTo(...getHealth(id)).withRel("health"),
    linkTo(...getMana(id)).withRel("mana"),
    linkTo(...getMoney(id)).withRel("money"),
    linkTo(...deleteAvatar(id)).withRel("delete")
);
```
This response acts as a **navigation hub**: a client that only knows the `/avatars/{id}` endpoint is able to reach the entire avatar API without additional documentation.


### Resources with contextual operations
Some sub-resources include links to the operations that make sense on them, making the response self-describing with respect to the current state.
**Money** — directly exposes the two available operations:
```java
linkTo(...earnMoney(id, null)).withRel("earn"),
linkTo(...spendMoney(id, null)).withRel("spend")
```

**Inventory** — links the modification operations and navigation towards equipped items:
```java
linkTo(...addItem(id, null)).withRel("addItem"),
linkTo(...removeItem(id, null)).withRel("removeItem"),
linkTo(...getEquippedItems(id)).withRel("equippedItems")
```

**Health** — exposes heal and damage as navigable actions:
```java
linkTo(...healAvatar(id, null)).withRel("heal"),
linkTo(...applyDamage(id, null)).withRel("damage")
```

**Stats** — provides links to the three available upgrades:
```java
linkTo(...increaseStrength(id)).withRel("increaseStrength"),
linkTo(...increaseDefense(id)).withRel("increaseDefense"),
linkTo(...increaseIntelligence(id)).withRel("increaseIntelligence")
```

### Collections (`GET /api/v1/avatars/search`)
Collective responses also use HATEOAS. Each element in the list includes a link to itself and to its own avatar, and the entire collection carries the `self` link that identifies the query that produced it:

```java
CollectionModel.of(
    avatarModels,
    linkTo(methodOn(AvatarController.class).searchAvatars(query)).withSelfRel()
);
```

## Structure of a HATEOAS Response
A typical response for `GET /api/v1/avatars/{id}/money` looks like this:

```json
{
  "amount": 150,
  "_links": {
    "self": { "href": "/api/v1/avatars/abc123" },
    "avatar": { "href": "/api/v1/avatars/abc123" },
    "earn": { "href": "/api/v1/avatars/abc123/money/earn" },
    "spend": { "href": "/api/v1/avatars/abc123/money/spend" }
  }
}
```