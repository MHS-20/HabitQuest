package habitquest.avatar_service.domain.events;

import habitquest.avatar_service.domain.avatar.Health;

public record HealthRestored(Health health) implements AvatarEvent {
}
