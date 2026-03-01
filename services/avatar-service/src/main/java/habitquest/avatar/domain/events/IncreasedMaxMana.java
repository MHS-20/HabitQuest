package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.AvatarMana;

public record IncreasedMaxMana(AvatarMana mana) implements AvatarEvent {}
