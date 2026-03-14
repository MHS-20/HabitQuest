package habitquest.guild.domain.battle;

import common.ddd.ValueObject;
import java.util.Objects;

public record Penalty(Integer amount) implements ValueObject {
  public Penalty {
    Objects.requireNonNull(amount);
    if (amount < 0) {
      throw new IllegalArgumentException("Money amount cannot be negative");
    }
  }
}
