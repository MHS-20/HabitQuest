package habitquest.avatar.domain.avatar;

import common.ddd.ValueObject;

public record Health(Integer value) implements ValueObject {
  public Health {
    if (value < 0) {
      throw new IllegalArgumentException("Health cannot be negative");
    }
  }
}
