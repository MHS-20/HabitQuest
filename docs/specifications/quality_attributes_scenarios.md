# Quality Attribute Scenarios for Habit RPG Microservices

## Edge Service – Availability

**Scenario:**  
- **Stimulus:** A login request arrives.  
- **Source:** Authenticated mobile or web client.  
- **Environment:** Normal operation, regular load.  
- **Artifact:** Edge Service API.  
- **Response:** Edge service must authenticate user and return a JWT.  
- **Response Measure:** ≤ 300 ms for 95% of authentication requests.

---

## Tracking Service – Performance

**Scenario:**  
- **Stimulus:** A user requests the list of active habits.  
- **Source:** Authenticated web or mobile user.  
- **Environment:** Under typical usage load.  
- **Artifact:** Tracking Service.  
- **Response:** Tracking service returns all active habits with correct metadata.  
- **Response Measure:** ≤ 500 ms for 95% of requests.

---

## Notification Service – Reliability

**Scenario:**  
- **Stimulus:** A scheduled reminder is due to be sent.  
- **Source:** Internal scheduler.  
- **Environment:** During normal system load.  
- **Artifact:** Notification Service.  
- **Response:** Notification Service issues the notification.  
- **Response Measure:** ≥ 99% of reminders delivered within 5 seconds after scheduled time.

---

## Avatar Service – Consistency

**Scenario:**  
- **Stimulus:** HabitCompleted event arrives via event bus.  
- **Source:** Habit Service.  
- **Environment:** Normal event processing conditions.  
- **Artifact:** Avatar Service.  
- **Response:** Avatar increments user XP and updates level if threshold hit.  
- **Response Measure:** XP updated within 1 second of event receipt.

---

## Avatar Service – Correctness

**Scenario:**  
- **Stimulus:** ItemPurchased event is received.  
- **Source:** Shop Service via event bus.  
- **Environment:** Normal asynchronous processing.  
- **Artifact:** Inventory Service.  
- **Response:** Inventory adds the purchased item to user inventory.  
- **Response Measure:** Inventory update reflected in queries within 1 second.

---

## Marketplace Service – Transactional Integrity

**Scenario:**  
- **Stimulus:** User submits an item purchase.  
- **Source:** Authenticated client.  
- **Environment:** Normal operating conditions.  
- **Artifact:** Marketplace Service.  
- **Response:** Marketplace service debits currency and issues ItemPurchased event.  
- **Response Measure:** Entire operation must complete successfully or fail atomically; no partial updates.

---

## Guild Service – Scalability

**Scenario:**  
- **Stimulus:** 50 users concurrently request to join a guild.  
- **Source:** Multiple authenticated users.  
- **Environment:** Elevated but expected load.  
- **Artifact:** Guild Service API.  
- **Response:** Guild service processes all join requests without failures.  
- **Response Measure:** ≥ 95% of requests handled within 1 second.
---

## Guild Service – Reliability

**Scenario:**  
- **Stimulus:** Guild combat action (attack or spell) arrives.  
- **Source:** Member of an active guild.  
- **Environment:** Combat session in progress.  
- **Artifact:** Combat Service.  
- **Response:** Combat service calculates damage and updates boss and player state.  
- **Response Measure:** Response delivered in ≤ 300 ms.

---

## Guild Service – Real-time

**Scenario:**  
- **Stimulus:** Guild member sends a chat message.  
- **Source:** Client via WebSocket.  
- **Environment:** Chat session active.  
- **Artifact:** Messaging Service.  
- **Response:** Message is broadcast to all members in the guild chat.  
- **Response Measure:** Delivery latency ≤ 200 ms.

## Quest Service – Responsiveness

**Scenario:**  
- **Stimulus:** User requests quest details.  
- **Source:** Authenticated client.  
- **Environment:** Normal load.  
- **Artifact:** Quest Service API.  
- **Response:** Quest service returns the quest data including associated habits and progress status.  
- **Response Measure:** ≤ 600 ms for 95% of requests.

---

