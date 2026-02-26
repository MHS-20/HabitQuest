package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.Health;

public record HealthRestored(Health health) implements AvatarEvent {}
