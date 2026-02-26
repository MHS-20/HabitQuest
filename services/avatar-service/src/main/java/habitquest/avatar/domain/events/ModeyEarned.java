package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.Money;

public record ModeyEarned(Money money) implements AvatarEvent {}
