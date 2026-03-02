package habitquest.marketplace.domain.items;

import habitquest.marketplace.domain.Money;

public record HealthPotion(BaseItem baseItem, Integer healingPower) implements Item, Potion {
  public HealthPotion(
      String name, String description, Integer healingPower, Money price, Level requiredLevel) {
    this(new BaseItem(name, description, price, requiredLevel), healingPower);
    if (healingPower < 0) {
      throw new IllegalArgumentException("Healing power cannot be negative");
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
    return healingPower;
  }

  public Money price() {
    return baseItem.price();
  }

  public Boolean canBuy(Level playerLevel) {
    return baseItem.canBuy(playerLevel);
  }
}
