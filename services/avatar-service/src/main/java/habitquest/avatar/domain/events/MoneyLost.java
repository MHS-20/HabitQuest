package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.Money;

public record MoneyLost(Money money) implements AvatarEvent {}
