package habitquest.avatar.domain.items;

import common.ddd.Aggregate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EquippedItems implements Aggregate<String> {
  private final String id;
  private List<Item> items;

  public EquippedItems(String id) {
    this.id = id;
    this.items = new ArrayList<>();
  }

  public List<Item> getItems() {
    return items;
  }

  public void equip(Item item) {
    Objects.requireNonNull(item);
    items.add(item);
  }

  public void unequip(Item item) {
    Objects.requireNonNull(item);
    if (!items.remove(item)) {
      throw new IllegalArgumentException("Item not found in inventory");
    }
  }

  @Override
  public String getId() {
    return this.id;
  }
}
