package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.AvatarHealth;

public record HealthLost(AvatarHealth health) implements AvatarEvent {}
