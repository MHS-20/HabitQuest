package habitquest.marketplace.domain.items;

import habitquest.marketplace.domain.Money;

public record Armor(BaseItem baseItem) implements Item {
  public Armor(String name, String description, Integer power, Money price, Level requiredLevel) {
    this(new BaseItem(name, description, power, price, requiredLevel));
  }

  @Override
  public ItemType itemType() {
    return ItemType.ARMOR;
  }
}
