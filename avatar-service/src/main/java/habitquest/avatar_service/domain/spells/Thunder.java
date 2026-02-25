package habitquest.avatar_service.domain.spells;

import habitquest.avatar_service.domain.avatar.Mana;

public record Thunder(String name, String description, Integer power, Mana requiredMana)
    implements Spell {}
