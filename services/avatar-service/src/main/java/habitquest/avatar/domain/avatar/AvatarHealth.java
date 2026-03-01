package habitquest.avatar.domain.avatar;

import common.ddd.ValueObject;

public record AvatarHealth(Health current, Health max) implements ValueObject {

  public AvatarHealth {
    if (max.value() <= 0) {
      throw new IllegalArgumentException("Max health must be positive");
    }
    if (current.value() < 0 || current.value() > max.value()) {
      throw new IllegalArgumentException("Current health must be between 0 and max");
    }
  }

  public AvatarHealth heal(Health other) {
    return new AvatarHealth(
        new Health(Math.min(current.value() + other.value(), max.value())), max);
  }

  public AvatarHealth damage(Health other) {
    return new AvatarHealth(new Health(Math.max(current.value() - other.value(), 0)), max);
  }

  public AvatarHealth resetHealth() {
    return new AvatarHealth(max, max);
  }

  public AvatarHealth increaseMax(Health other) {
    return new AvatarHealth(
        new Health(max.value() + other.value()), new Health(max.value() + other.value()));
  }

  public boolean isDead() {
    return current.value() == 0;
  }
}
