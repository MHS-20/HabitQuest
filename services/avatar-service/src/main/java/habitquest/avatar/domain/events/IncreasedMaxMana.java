package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.Mana;

public record IncreasedMaxMana(Mana mana) implements AvatarEvent {}
