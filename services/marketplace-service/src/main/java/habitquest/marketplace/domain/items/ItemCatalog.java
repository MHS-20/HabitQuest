package habitquest.marketplace.domain.items;

import habitquest.marketplace.domain.marketplace.Money;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemCatalog {

  private final Map<String, Item> items =
      Map.of(
          "Iron Sword",
          new Weapon("Iron Sword", "A basic sword", 10, new Money(50), new Level(1)),
          "Iron Shield",
          new Armor("Iron Shield", "A basic shield", 5, new Money(30), new Level(1)),
          "HP Potion",
          new HealthPotion("HP Potion", "Restores HP", 50, new Money(10), new Level(1)),
          "MP Potion",
          new ManaPotion("MP Potion", "Restores MP", 30, new Money(12), new Level(1)));

  public Optional<Item> getItem(Item item) {
    return Optional.ofNullable(items.get(item.name()));
  }

  public Optional<Item> getItemByName(String name) {
    return Optional.ofNullable(items.get(name));
  }

  public boolean contains(Item item) {
    return items.containsKey(item.name());
  }

  public List<Item> getItemsByType(ItemFilter type) {
    return items.values().stream().filter(type.predicate()).toList();
  }

  public List<Item> getAllItems() {
    return items.values().stream().toList();
  }
}
