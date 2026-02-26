package habitquest.marketplace.domain.items;

import habitquest.marketplace.domain.Level;
import habitquest.marketplace.domain.Money;

public record Armor(BaseItem baseItem, Integer defensePower) implements Item {
  public Armor(
      String name, String description, Integer defensePower, Money price, Level requiredLevel) {
    this(new BaseItem(name, description, price, requiredLevel), defensePower);
    if (defensePower < 0) {
      throw new IllegalArgumentException("Defense power cannot be negative");
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
    return defensePower;
  }

  public Money price() {
    return baseItem.price();
  }

  public Boolean canBuy(Level playerLevel) {
    return baseItem.canBuy(playerLevel);
  }
}
