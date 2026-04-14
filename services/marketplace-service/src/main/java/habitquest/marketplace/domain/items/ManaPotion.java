package habitquest.marketplace.domain.items;

import habitquest.marketplace.domain.marketplace.Money;

public record ManaPotion(BaseItem baseItem) implements Potion {
  public ManaPotion(
      String name, String description, Integer power, Money price, Level requiredLevel) {
    this(new BaseItem(name, description, power, price, requiredLevel));
  }
}
