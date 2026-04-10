package habitquest.marketplace.domain.items;

import common.ddd.ValueObject;
import habitquest.marketplace.domain.Money;

public sealed interface Item extends ValueObject permits Weapon, Armor, Potion {

  BaseItem baseItem();

  ItemType itemType();

  default String name() {
    return baseItem().name();
  }

  default String description() {
    return baseItem().description();
  }

  default int power() {
    return baseItem().power();
  }

  default Money price() {
    return baseItem().price();
  }

  default Level requiredLevel() {
    return baseItem().requiredLevel();
  }

  default Boolean canBuy(Level playerLevel) {
    return baseItem().canBuy(playerLevel);
  }
}
