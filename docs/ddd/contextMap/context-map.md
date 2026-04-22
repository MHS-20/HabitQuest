# Context Map
**Avatar** is the central bounded context and acts as an upstream for most of the system.  
It exposes a stable REST API that all other contexts consume, hiding its real internal representation, so it's considered an **Open Host Service (OHS)**.

The mapped relationships are:

- **Edge → Avatar** — Conformist [D] / OHS [U]: During registration, the Edge calls the Avatar sending the user ID for Avatar creation. Edge adapts to Avatar’s model.

- **Edge → Notification** — Customer-Supplier[U→D]: AuthService uses UserNotifier to trigger a registration notification. This is a unidirectional relationship where Notification is a stable supplier.

- **Avatar → Marketplace** — Customer-Supplier [U→D]: During avatar creation, a corresponding Marketplace is created. Avatar drives the Marketplace lifecycle.

- **Marketplace → Avatar** — Anti-Corruption Layer (ACL) [D] / OHS [U]: Marketplace translates its internal concepts of Money and Item into Avatar calls via the port interface. The port acts as an anti-corruption layer isolating the internal model.

- **Guild → Avatar** — Anti-Corruption Layer (ACL) [D] / OHS [U]: The Guild translate battle/guild concepts (damage, rewards, invitations) into calls to the Avatar model.

- **Tracking → Avatar** — Customer-Supplier [D] / OHS [U]: Tracking send calls to grant experience and apply damage on Avatar. No significant translation, just a simple downstream dependency.

- **Tracking ⟷ Quest** — Partners: Coordinated bidirectional integration. Tracking calls to record habit attendance when a habit is completed. Quest calls to create quest habits, when an avatar joins a quest.

- **Quest → Avatar** — Customer-Supplier [D] / OHS [U]: Quest calls the avatar to earn money on completion, and calls apply damage when a quest expires.


![Context Map](./context-map.png)
