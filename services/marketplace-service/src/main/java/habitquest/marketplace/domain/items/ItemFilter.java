package habitquest.marketplace.domain.items;

import java.util.function.Predicate;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public enum ItemFilter {
  ALL,
  ARMOR,
  WEAPON,
  POTION,
  HEALTH_POTION,
  MANA_POTION;

  public Predicate<Item> predicate() {
    return switch (this) {
      case ALL -> item -> true;
      case ARMOR -> item -> item instanceof Armor;
      case WEAPON -> item -> item instanceof Weapon;
      case POTION -> item -> item instanceof Potion;
      case HEALTH_POTION -> item -> item instanceof HealthPotion;
      case MANA_POTION -> item -> item instanceof ManaPotion;
    };
  }
}
