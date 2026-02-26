package habitquest.avatar.domain.spells;

import common.ddd.ValueObject;
import habitquest.avatar.domain.avatar.Mana;

public interface Spell extends ValueObject {
  String name();

  String description();

  Mana requiredMana();

  Integer power();
}
