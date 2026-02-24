package habitquest.avatar_service.domain.events;

import habitquest.avatar_service.domain.avatar.Health;

public record HealthLost(Health health) implements AvatarEvent {
}
