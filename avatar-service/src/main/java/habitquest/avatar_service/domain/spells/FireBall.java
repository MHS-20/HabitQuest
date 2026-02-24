package habitquest.avatar_service.domain.spells;

import habitquest.avatar_service.domain.avatar.Mana;

public record FireBall(Integer damage, Mana requiredMana) implements Spell {
}
