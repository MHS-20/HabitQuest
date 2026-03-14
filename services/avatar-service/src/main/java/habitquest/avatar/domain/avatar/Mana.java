package habitquest.avatar.domain.avatar;

import common.ddd.ValueObject;

public record Mana(Integer value) implements ValueObject {
  public Mana {
    if (value < 0) {
      throw new IllegalArgumentException("Mana cannot be negative");
    }
  }
}
