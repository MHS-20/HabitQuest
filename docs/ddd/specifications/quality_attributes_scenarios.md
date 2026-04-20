# Quality Attribute Scenarios

## Edge Service — Availability (Login)

- **Stimulus:** A login request arrives at the system.
- **Source:** Mobile or web client.
- **Environment:** Normal operation, regular load.
- **Artifact:** Edge Service — authentication endpoint.
- **Response:** The system verifies the user's credentials, generates a session token, and returns it to the client.
- **Response Measure:** ≤ 300 ms for 95% of requests.

---

## Edge Service — Correctness (Registration)

- **Stimulus:** A registration request arrives at the system.
- **Source:** New user from a mobile or web client.
- **Environment:** Normal operation.
- **Artifact:** Edge Service — registration endpoint.
- **Response:** The system creates the user account and the associated avatar profile as an atomic operation. If either creation fails, the entire operation is rolled back and an explicit error is returned.
- **Response Measure:** No partial registration is observable by the client: either both resources are created, or neither is.

---

## Tracking Service — Performance

- **Stimulus:** A user requests the list of habits associated with their avatar.
- **Source:** Authenticated user from a web or mobile client.
- **Environment:** Typical usage load.
- **Artifact:** Tracking Service — habit query endpoint.
- **Response:** The system returns all habits belonging to the avatar, along with their full metadata.
- **Response Measure:** ≤ 500 ms for 95% of requests.

---

## Avatar Service — Consistency (Experience Grant)

- **Stimulus:** A user completes a habit.
- **Source:** Tracking Service.
- **Environment:** Normal operation, synchronous processing.
- **Artifact:** Avatar Service — experience update endpoint.
- **Response:** The system updates the avatar's experience points, checks for level-up conditions, and unlocks new abilities if applicable. State is persisted before any downstream notification is triggered.
- **Response Measure:** State persisted within 500 ms of the request.

---

## Marketplace Service — Transactional Integrity

- **Stimulus:** A user purchases an item in the marketplace.
- **Source:** Authenticated client.
- **Environment:** Normal operating conditions.
- **Artifact:** Marketplace Service — item purchase endpoint.
- **Response:** The system deducts the required currency from the avatar, adds the item to the inventory, and updates the marketplace. If any step fails, all previously completed operations are compensated and an explicit error is returned.
- **Response Measure:** No partial update is observable by the client: the purchase either succeeds completely or is fully rolled back.

---

## Avatar Service — Inventory Update

- **Stimulus:** A purchase in the Marketplace is confirmed.
- **Source:** Marketplace Service.
- **Environment:** Synchronous processing within a purchase flow.
- **Artifact:** Avatar Service — inventory management endpoint.
- **Response:** The system adds the purchased item to the avatar's inventory and persists the updated state.
- **Response Measure:** The updated inventory is reflected in subsequent queries within 500 ms of the request.

---

## Guild Service — Scalability (Invite Acceptance)

- **Stimulus:** 50 users concurrently submit an invite acceptance request to the same guild.
- **Source:** Multiple authenticated users.
- **Environment:** High but expected load.
- **Artifact:** Guild Service — invite acceptance endpoint.
- **Response:** The system handles concurrent requests while preserving guild state consistency (no duplicate members, no lost updates).
- **Response Measure:** ≥ 95% of requests completed successfully within 1 second.

---

## Battle Service — Reliability (Combat Action)

- **Stimulus:** A guild member performs an attack against the boss.
- **Source:** Member of a guild with an active battle.
- **Environment:** Active combat session.
- **Artifact:** Battle Service — damage processing endpoint.
- **Response:** The system verifies that it is the player's turn, computes the action outcome (ongoing, victory, or defeat), and updates the state of all involved avatars accordingly, distributing rewards or applying penalties.
- **Response Measure:** Response to the combat action delivered within ≤ 300 ms.

---

## Quest Service — Responsiveness

- **Stimulus:** A user requests quest details or their current progress.
- **Source:** Authenticated client.
- **Environment:** Normal load.
- **Artifact:** Quest Service — quest detail and progress endpoints.
- **Response:** The system returns the requested quest data. For progress queries, it refreshes the status of active quests, applies any expiry penalties, and computes the completion percentage for each associated habit.
- **Response Measure:** ≤ 600 ms for quest detail requests; ≤ 800 ms for progress requests (due to state refresh and interactions with the Avatar Service).