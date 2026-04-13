# Quality Attributes – Habit Tracking RPG

## 1. Edge Service
- In case of internal error, it must return a valid HTTP response (4xx/5xx) within 2 seconds.
- The average response time for login/registration must be ≤ 500 ms.
- 95% of requests must complete within 1 second.

---

## 2. Tracking Service
- Creation or update of a habit ≤ 500 ms in 95% of cases.
- Retrieval of user habit list ≤ 700 ms in 95% of cases.
- After a habit completion, the updated state must be visible within 1 second.
- No duplication of habits for the same user with the same name.
- In case of database error, the system must not lose already confirmed data.

---

## 3. Avatar
- An item cannot have a negative quantity.
- Every add/remove operation must be atomic.
- User inventory retrieval ≤ 500 ms in 95% of cases.
- Item quantity update ≤ 500 ms in 95% of cases.
- After a modification, the new inventory state must be consistent across all subsequent calls.

---

## 4. Marketplace Service
- Item purchase or sale ≤ 700 ms in 95% of cases.
- Retrieval of shop item list ≤ 500 ms.
- In a sale: currency increase and item removal must occur in the same transaction.
- If either operation fails, no changes must be applied.
- It is not possible to sell an item not present in the inventory.
- The user balance cannot become negative after a purchase.

---

## 5. Tracking Service
- XP reward calculation ≤ 500 ms.
- User level update ≤ 500 ms in 95% of cases.
- No double assignment of XP for the same event.
- Already processed events must not be reprocessed.

---

## 6. Notification Service
- Notification creation ≤ 300 ms.
- Asynchronous notification delivery within 5 seconds of generation.
- In case of delivery failure, at least 1 retry attempt must be provided.
- Failed notifications must be logged.