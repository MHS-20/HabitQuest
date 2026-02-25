package habitquest.avatar_service.domain.avatar;

import common.ddd.ValueObject;
import java.util.Objects;

public record Mana(Integer amount, Integer max) implements ValueObject {
  public Mana {
    Objects.requireNonNull(amount);
    Objects.requireNonNull(max);
    if (amount < 0) {
      throw new IllegalArgumentException("Mana amount cannot be negative");
    }
  }

  public Mana add(Mana other) {
    Objects.requireNonNull(other);
    return new Mana(Math.min(amount + other.amount, max), max);
  }

  public Mana subtract(Mana other) {
    Objects.requireNonNull(other);
    if (this.amount < other.amount) {
      throw new IllegalArgumentException("Cannot subtract more mana than available");
    }
    return new Mana(this.amount - other.amount, max);
  }

  public Mana resetMana() {
    return new Mana(0, max);
  }
}
