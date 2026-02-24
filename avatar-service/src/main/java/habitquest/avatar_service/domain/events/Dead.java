package habitquest.avatar_service.domain.events;

import habitquest.avatar_service.domain.avatar.Health;

public record Dead(Health health) implements AvatarEvent {
}
