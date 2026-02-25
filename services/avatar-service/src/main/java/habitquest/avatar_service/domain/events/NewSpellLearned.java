package habitquest.avatar_service.domain.events;

import habitquest.avatar_service.domain.spells.Spell;

public record NewSpellLearned(Spell spell) implements AvatarEvent {
}
