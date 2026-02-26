package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.Health;

public record IncreasedMaxHp(Health health) implements AvatarEvent {}
