package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.AvatarHealth;

public record IncreasedMaxHp(AvatarHealth health) implements AvatarEvent {}
