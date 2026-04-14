package habitquest.marketplace.domain.items;

import habitquest.marketplace.domain.marketplace.Money;

public record HealthPotion(BaseItem baseItem) implements Potion {
  public HealthPotion(
      String name, String description, Integer power, Money price, Level requiredLevel) {
    this(new BaseItem(name, description, power, price, requiredLevel));
  }
}
