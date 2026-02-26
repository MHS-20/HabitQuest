package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.Mana;

public record ManaRestored(Mana mana) implements AvatarEvent {}
