package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.Health;

public record Dead(Health health) implements AvatarEvent {}
