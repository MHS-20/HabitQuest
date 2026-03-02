package habitquest.marketplace.domain.items;

import habitquest.marketplace.domain.Money;

public record ManaPotion(BaseItem baseItem, Integer restoringPower) implements Item {
  public ManaPotion(
      String name, String description, Integer restoringPower, Money price, Level requiredLevel) {
    this(new BaseItem(name, description, price, requiredLevel), restoringPower);
    if (restoringPower < 0) {
      throw new IllegalArgumentException("Mana power cannot be negative");
    }
  }

  @Override
  public String name() {
    return baseItem.name();
  }

  @Override
  public String description() {
    return baseItem.description();
  }

  public Level requiredLevel() {
    return baseItem.requiredLevel();
  }

  public Integer getValue() {
    return restoringPower;
  }

  public Money price() {
    return baseItem.price();
  }

  public Boolean canBuy(Level playerLevel) {
    return baseItem.canBuy(playerLevel);
  }
}
