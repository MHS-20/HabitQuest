package habitquest.avatar_service.domain;

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

  public Health heal(Integer amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Cannot heal negative amount");
    }
    return new Health(Math.min(current + amount, max), max);
  }

  public Health damage(Integer amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Cannot damage negative amount");
    }
    return new Health(Math.max(current - amount, 0), max);
  }

  public boolean isDead() {
    return current == 0;
  }
}
