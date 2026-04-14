package habitquest.marketplace.domain.items;

import habitquest.marketplace.domain.marketplace.Money;
import java.util.Objects;

public record BaseItem(
    String name, String description, Integer power, Money price, Level requiredLevel) {

  public static final int MIN_LEVEL = 1;
  public static final int MIN_PRICE = 0;
  public static final int MIN_POWER = 0;

  public BaseItem {
    Objects.requireNonNull(name);
    Objects.requireNonNull(description);
    Objects.requireNonNull(price);
    Objects.requireNonNull(requiredLevel);

    if (name.isBlank()) {
      throw new IllegalArgumentException("Item name cannot be null or blank");
    }

    if (description.isBlank()) {
      throw new IllegalArgumentException("Item description cannot be null or blank");
    }

    if (power < MIN_POWER) {
      throw new IllegalArgumentException("Item power cannot be negative");
    }

    if (price.amount() < MIN_PRICE) {
      throw new IllegalArgumentException("Item price cannot be negative");
    }

    if (requiredLevel.levelNumber() < MIN_LEVEL) {
      throw new IllegalArgumentException("Item required level must be at least 1");
    }
  }

  public Boolean canBuy(Level playerLevel) {
    return playerLevel.levelNumber() >= requiredLevel.levelNumber();
  }
}
