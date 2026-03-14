package habitquest.marketplace.domain.items;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;
import java.util.function.Predicate;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public enum ItemType {
  ALL(item -> true),
  ARMOR(item -> item instanceof Armor),
  WEAPON(item -> item instanceof Weapon),
  POTION(item -> item instanceof Potion),
  HEALTH_POTION(item -> item instanceof HealthPotion),
  MANA_POTION(item -> item instanceof ManaPotion);

  private final Predicate<Item> filter;

  ItemType(Predicate<Item> filter) {
    this.filter = filter;
  }

  public Predicate<Item> filter() {
    return filter;
  }

  @JsonCreator
  public static ItemType fromString(String value) {
    return valueOf(value.toUpperCase(Locale.getDefault()));
  }
}
