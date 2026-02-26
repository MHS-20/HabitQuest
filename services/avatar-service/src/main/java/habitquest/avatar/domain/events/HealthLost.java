package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.Health;

public record HealthLost(Health health) implements AvatarEvent {}
