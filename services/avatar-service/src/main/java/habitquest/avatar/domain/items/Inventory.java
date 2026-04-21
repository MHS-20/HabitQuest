package habitquest.avatar.domain.items;

import common.ddd.Entity;
import common.ddd.Id;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Inventory implements Entity<Id<Inventory>> {
  private final Id<Inventory> id;
  private final List<Item> items;

  public Inventory(Id<Inventory> id) {
    this.id = id;
    this.items = new ArrayList<>();
  }

  public Inventory(Id<Inventory> id, List<Item> items) {
    this.id = id;
    this.items = new ArrayList<>(items);
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
  public Id<Inventory> getId() {
    return this.id;
  }
}
