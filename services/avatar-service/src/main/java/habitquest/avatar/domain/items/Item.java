package habitquest.avatar.domain.items;

import common.ddd.ValueObject;

public sealed interface Item extends ValueObject permits Weapon, Armor, Potion {

  BaseItem baseItem();

  default String name() {
    return baseItem().name();
  }

  default String description() {
    return baseItem().description();
  }

  default int power() {
    return baseItem().power();
  }
}
