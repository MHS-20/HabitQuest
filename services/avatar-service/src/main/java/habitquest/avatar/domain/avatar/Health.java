package habitquest.avatar.domain.avatar;

import common.ddd.ValueObject;

public record Health(Integer current, Integer max) implements ValueObject {

  public Health {
    if (max <= 0) {
      throw new IllegalArgumentException("Max health must be positive");
    }
    if (current < 0 || current > max) {
      throw new IllegalArgumentException("Current health must be between 0 and max");
    }
  }

  public Health heal(Health other) {
    return new Health(Math.min(current + other.current, max), max);
  }

  public Health damage(Health other) {
    return new Health(Math.max(current - other.current, 0), max);
  }

  public boolean isDead() {
    return current == 0;
  }
}
