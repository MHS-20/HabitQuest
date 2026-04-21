package habitquest.avatar.domain.avatar;

import common.ddd.ValueObject;

public record Damage(Integer value) implements ValueObject {
  public Damage {
    if (value < 0) {
      throw new IllegalArgumentException("Damage cannot be negative");
    }
  }
}
