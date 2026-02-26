package habitquest.avatar.domain.events;

import habitquest.avatar.domain.spells.Spell;

public record NewSpellLearned(Spell spell) implements AvatarEvent {}
