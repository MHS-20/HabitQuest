package habitquest.avatar.domain.events;

import common.ddd.Id;
import habitquest.avatar.domain.avatar.Avatar;
import habitquest.avatar.domain.spells.Spell;

public record NewSpellLearned(Id<Avatar> avatarId, Spell spell) implements AvatarEvent {}
