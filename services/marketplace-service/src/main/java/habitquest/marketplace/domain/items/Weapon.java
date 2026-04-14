package habitquest.marketplace.domain.items;

import habitquest.marketplace.domain.marketplace.Money;

public record Weapon(BaseItem baseItem) implements Equipment {
  public Weapon(String name, String description, Integer power, Money price, Level requiredLevel) {
    this(new BaseItem(name, description, power, price, requiredLevel));
  }
}
