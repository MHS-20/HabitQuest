package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.AvatarHealth;

public record HealthRestored(AvatarHealth health) implements AvatarEvent {}
