package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.AvatarMana;

public record ManaRestored(AvatarMana mana) implements AvatarEvent {}
