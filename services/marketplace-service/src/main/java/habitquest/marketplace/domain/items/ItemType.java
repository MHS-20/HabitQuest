package habitquest.marketplace.domain.items;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;
import java.util.function.Predicate;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public enum ItemType {
  ALL,
  ARMOR,
  WEAPON,
  POTION,
  HEALTH_POTION,
  MANA_POTION;

  public Predicate<Item> filter() {
    if (this == ALL) {
      return item -> true;
    }
    return item -> item.itemType() == this;
  }

  @JsonCreator
  public static ItemType fromString(String value) {
    return valueOf(value.toUpperCase(Locale.ROOT));
  }
}
