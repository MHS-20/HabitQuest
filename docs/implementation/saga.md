# Saga Management in `MarketplaceController`
In the Marketplace controller, the purchase (`buyItem`) and sale (`sellItem`) operations coordinate two distinct systems:
the `AvatarClient` service (external system) and the `MarketplaceService` (local domain).

## Purchase saga (`buyItem`)
The operation is organized into three sequential steps, with two boolean flags — `moneySpent` and `inventoryAdded` — that track the progress state in order to build the exact rollback.

- **Step 0 — pre-condition:** before starting the saga, it is verified that the avatar has a sufficient level via `canBuyItem()`. This is a synchronous guard: if it fails, a `403 Forbidden` is returned immediately without touching any system.
- **Step 1 — `spendMoney`:** `avatarClient.spendMoney()` is called. If successful, the `moneySpent` flag is set to `true`.
- **Step 2 — `addItemToInventory`:** the item is added to the avatar's inventory. If successful, `inventoryAdded` becomes `true`.
- **Step 3 — `buyItem`:** only after completing both remote operations, the marketplace is updated locally via `marketplaceService.buyItem()`.

**Compensation (rollback)**: if any step throws a `RestClientException` or `AvatarCommunicationException`,
the `catch` block performs **selective** rollback using the flags:

- if `inventoryAdded` is `true`: calls `removeItemFromInventory`
- if `moneySpent` is `true`: calls `earnMoney`

The compensation itself can fail (nested catch):
in that case an exception is raised with a message signaling the residual inconsistency, and the caller receives `502 Bad Gateway`.

## Sale saga (`sellItem`)
Same structure, reversed steps.

- **Step 1 — `removeItemFromInventory`:** the item is removed from the avatar's inventory. If successful, `removedFromInventory` becomes `true`.
- **Step 2 — `earnMoney`:** the avatar receives the money from the sale. If successful, `earnedMoney` becomes `true`.
- **Step 3 — `sellItem`:** the marketplace is updated locally via `marketplaceService.sellItem()`.

**Compensation (rollback)**: if any step throws a `RestClientException` or `AvatarCommunicationException`, the `catch` block performs **selective** rollback using the flags:

- if `earnedMoney` is `true` → calls `spendMoney`
- if `removedFromInventory` is `true` → calls `addItemToInventory`


## Guarantees and limits of the solution
| Property | Value |
|---|---|
| Saga type | Choreography-based with inline orchestration in the controller |
| Guaranteed consistency | Eventual consistency — no strong atomicity |
| Idempotency | Not guaranteed |
| Durability | None — a crash during compensation leaves the state inconsistent |
| Logging | Every step is tracked via `MarketplaceLogger` |

The main weak point is the absence of **saga persistence**: if the server crashes between one step and the next, no automatic mechanism resumes the compensation.
In a production system one would consider the use of a persistent saga log, or delegate orchestration to a message broker with delivery guarantees.