# Quality Attributes — Habit RPG Microservices

---

## General (all services)

### Scalability

- Each microservice must be independently horizontally scalable by adding replicas without any code change or coordination between instances.
- Stateless design: no microservice may store user session state in memory; all persistent state must reside in the database.

### Availability

- Each microservice must be available ≥ 99.5% of the time, measured on a monthly rolling window.

---

## 1. Edge Service

### Performance

- Average response time for login and registration must be ≤ 500 ms.
- 95% of login and registration requests must complete within 1 second.
- In case of an internal error, the service must return a valid HTTP response (4xx/5xx) within 2 seconds.

### Correctness
- Input validation (email format, password minimum length, non-blank fields) must be enforced at the API boundary before any business logic is executed.
- Duplicate email addresses must be rejected before any resource is created.

### Security
- The JWT must be validated for signature, expiry, and issuer on every protected request.
- Passwords must never be stored in plain text; only the hashed form is persisted.

---

## 2. Avatar Service

### Performance

- Retrieval of any avatar attribute must complete within 500 ms in 95% of cases.
- Any inventory mutation (add, remove, equip, unequip item) must complete within 500 ms in 95% of cases.
- Any stat or currency mutation (spend money, earn money, apply damage, grant experience, spend mana) must complete within 500 ms in 95% of cases.

### Consistency

- Every mutation (inventory, currency, health, experience, stats) must be performed as an atomic read-modify-write on the avatar aggregate
- After a successful mutation, the new state must be immediately visible to all subsequent read requests.

### Correctness

- Avatar creation also triggers the creation of a corresponding marketplace. If that call fails, the avatar creation must be considered failed.
- Item operations (equip, unequip, use potion) must enforce domain invariants (e.g. item must exist in inventory before it can be used or equipped); violations must result in a 4xx error.

---

## 3. Tracking Service

### Performance

- Creation or update of a habit must complete within 500 ms in 95% of cases.
- Retrieval of the habit list for an avatar must complete within 700 ms in 95% of cases.

### Consistency
- Habit state (title, description, tags, recurrence, last attended date) is persisted atomically before any downstream call or event notification is triggered.

---

## 4. Marketplace Service

### Performance

- Item purchase or sale must complete within 700 ms in 95% of cases.
- Retrieval of available or sold item lists must complete within 500 ms in 95% of cases.

### Consistency (saga compensation)
- A purchase involves three sequential calls to the Avatar Service: deduct currency, add item to inventory, and then update the marketplace state locally. Atomicity must be guaranteed.
- If adding the item to inventory fails after currency has been deducted, the currency deduction is compensated by crediting it back before the error is propagated.
- If any step fails, all previously completed steps must be compensated and an explicit error must be returned. No partial state (money deducted but item not added, or vice versa) must be observable by the client after the error response.
- A sale follows the same pattern in reverse: the item is removed from inventory first, then currency is credited. Compensation restores the item if the currency credit fails.

---

## 5. Guild Service

### Performance

- Guild creation must complete within 500 ms in 95% of cases.
- Guild information retrieval must complete within 700 ms in 95% of cases.

### Consistency

- Guild membership changes (join, leave, remove, promote) must be performed as atomic read-modify-write.
- After a membership change, the new state must be immediately visible to all subsequent read requests.
- When a member accepts an invite, if a battle is currently in progress for that guild, the battle's turn count is incremented as part of the same operation.
- When a member leaves or is removed, if a battle is in progress, the battle's turn count is decremented. If the guild becomes empty as a result, the battle and the guild are both deleted.


### Correctness
- Role promotion and member removal enforce role-based authorization rules at the domain level before any state change is persisted.

---

## 6. Battle Service

### Performance

- Response to a battle action (damage) must be delivered within 300 ms in 95% of cases.
- Battle state queries (current turn, boss health, attacker turn check) must complete within 300 ms in 95% of cases.

### Correctness
- A damage action is only accepted if it is the attacker's turn; requests out of turn must be rejected before any state mutation.

### Consistency
- Turn management is consistent with guild membership: joining a guild increments the turn count, leaving or being removed decrements it, ensuring the turn rotation always reflects the current member list.

---

## 7. Quest Service

### Performance

- Quest detail retrieval must complete within 600 ms in 95% of cases.
- Joining a quest must complete within 800 ms in 95% of cases.

### Consistency

- Active quest state (attendance records, status) is persisted atomically before any reward or penalty call is issued.
- Progress responses always reflect the most up-to-date status as of the time of the request, including any status transitions and penalty applications triggered during the retrieval.

---

## 8. Notification Service

### Performance
- Notification dispatch must be initiated within 500 ms of the triggering event.

### Correctness
- Notification failures must not affect the outcome of the business operation that triggered them.