package habitquest.avatar.domain.items;

import common.ddd.Entity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Inventory implements Entity<String> {
  private final String id;
  private List<Item> items;

  public Inventory(String id) {
    this.id = id;
    this.items = new ArrayList<>();
  }

  public List<Item> getItems() {
    return Collections.unmodifiableList(items);
  }

  public void addItem(Item item) {
    Objects.requireNonNull(item);
    items.add(item);
  }

  public void removeItem(Item item) {
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
