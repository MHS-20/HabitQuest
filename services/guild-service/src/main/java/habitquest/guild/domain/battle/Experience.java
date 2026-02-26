package habitquest.guild.domain.battle;

import common.ddd.ValueObject;
import java.util.Objects;

public record Experience(Integer amount) implements ValueObject, Comparable<Experience> {
  public Experience {
    Objects.requireNonNull(amount);
    if (amount < 0) {
      throw new IllegalArgumentException("Experience amount cannot be negative");
    }
  }

  public Experience add(Experience other) {
    Objects.requireNonNull(other);
    return new Experience(this.amount + other.amount);
  }

  public Experience resetExperience() {
    return new Experience(0);
  }

  @Override
  public int compareTo(Experience other) {
    Objects.requireNonNull(other);
    return Integer.compare(amount(), other.amount());
  }

  public boolean isAtLeast(Experience other) {
    Objects.requireNonNull(other);
    return compareTo(other) >= 0;
  }

  public boolean isGreaterThan(Experience other) {
    Objects.requireNonNull(other);
    return compareTo(other) > 0;
  }

  public boolean isLessThan(Experience other) {
    Objects.requireNonNull(other);
    return compareTo(other) < 0;
  }
}
