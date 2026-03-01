package habitquest.avatar.domain.avatar;

import common.ddd.ValueObject;
import java.util.Objects;

public record AvatarMana(Mana amount, Mana max) implements ValueObject {
  public AvatarMana {
    Objects.requireNonNull(amount);
    Objects.requireNonNull(max);
    if (amount.value() < 0) {
      throw new IllegalArgumentException("Mana amount cannot be negative");
    }
  }

  public AvatarMana add(Mana other) {
    Objects.requireNonNull(other);
    return new AvatarMana(new Mana(Math.min(amount.value() + other.value(), max.value())), max);
  }

  public AvatarMana subtract(Mana other) {
    Objects.requireNonNull(other);
    if (this.amount.value() < other.value()) {
      throw new IllegalArgumentException("Cannot subtract more mana than available");
    }
    return new AvatarMana(new Mana(this.amount.value() - other.value()), max);
  }

  public AvatarMana resetMana() {
    return new AvatarMana(max, max);
  }

  public AvatarMana increaseMax(Mana other) {
    Objects.requireNonNull(other);
    return new AvatarMana(
        new Mana(max.value() + other.value()), new Mana(max.value() + other.value()));
  }
}
