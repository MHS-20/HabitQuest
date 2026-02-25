package habitquest.avatar_service.domain.spells;

import common.ddd.ValueObject;
import habitquest.avatar_service.domain.avatar.Mana;

public interface Spell extends ValueObject {
    String name();
    String description();
    Mana requiredMana();
    Integer power();
}
