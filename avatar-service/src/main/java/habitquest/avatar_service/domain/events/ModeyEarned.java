package habitquest.avatar_service.domain.events;

import habitquest.avatar_service.domain.avatar.Money;

public record ModeyEarned(Money money) implements AvatarEvent {}
