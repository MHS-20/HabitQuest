package habitquest.avatar.domain.items;

import common.ddd.Aggregate;
import java.util.List;
import java.util.Objects;

public class EquippedItems implements Aggregate<String> {
  private final String id;
  private List<Item> items;

  public List<Item> getItems() {
    return items;
  }

  public EquippedItems(String id) {
    this.id = id;
  }

  public void equipItem(Item item) {
    Objects.requireNonNull(item);
    items.add(item);
  }

  public void unequipItem(Item item) {
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
