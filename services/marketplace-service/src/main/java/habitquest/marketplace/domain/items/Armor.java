package habitquest.marketplace.domain.items;

import habitquest.marketplace.domain.Money;

public record Armor(BaseItem baseItem) implements Equipment {
  public Armor(String name, String description, Integer power, Money price, Level requiredLevel) {
    this(new BaseItem(name, description, power, price, requiredLevel));
  }
}
