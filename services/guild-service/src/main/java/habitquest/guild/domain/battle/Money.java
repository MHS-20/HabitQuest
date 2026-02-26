package habitquest.guild.domain.battle;

import common.ddd.ValueObject;
import java.util.Objects;

public record Money(Integer amount) implements ValueObject {
  public Money {
    Objects.requireNonNull(amount);
    if (amount < 0) {
      throw new IllegalArgumentException("Money amount cannot be negative");
    }
  }

  public Money add(Money other) {
    return new Money(this.amount + other.amount);
  }

  public Money subtract(Money other) {
    if (this.amount < other.amount) {
      throw new IllegalArgumentException("Cannot subtract more money than available");
    }
    return new Money(this.amount - other.amount);
  }
}
