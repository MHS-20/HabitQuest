package habitquest.avatar.domain.spells;

import habitquest.avatar.domain.avatar.Mana;

public record FireBall(String name, String description, Integer power, Mana requiredMana)
    implements Spell {}
