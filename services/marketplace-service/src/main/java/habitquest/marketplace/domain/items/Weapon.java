package habitquest.marketplace.domain.items;

import habitquest.marketplace.domain.Money;

public record Weapon(BaseItem baseItem, Integer attackPower) implements Item {
  public Weapon(
      String name, String description, Integer attackPower, Money price, Level requiredLevel) {
    this(new BaseItem(name, description, price, requiredLevel), attackPower);
    if (attackPower < 0) {
      throw new IllegalArgumentException("Attack power cannot be negative");
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
    return attackPower;
  }

  public Money price() {
    return baseItem.price();
  }

  public Boolean canBuy(Level playerLevel) {
    return baseItem.canBuy(playerLevel);
  }
}
