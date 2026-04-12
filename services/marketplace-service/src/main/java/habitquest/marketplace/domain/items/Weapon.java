package habitquest.marketplace.domain.items;

import habitquest.marketplace.domain.Money;

public record Weapon(BaseItem baseItem) implements Item {
  public Weapon(String name, String description, Integer power, Money price, Level requiredLevel) {
    this(new BaseItem(name, description, power, price, requiredLevel));
  }
}
