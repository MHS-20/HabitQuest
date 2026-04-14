package habitquest.avatar.domain.items;

import common.ddd.Entity;
import common.ddd.Id;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EquippedItems implements Entity<Id<EquippedItems>> {
  private final Id<EquippedItems> id;
  private List<Equipment> items;

  public EquippedItems(Id<EquippedItems> id) {
    this.id = id;
    this.items = new ArrayList<>();
  }

  public List<Equipment> getItems() {
    return Collections.unmodifiableList(items);
  }

  public void equip(Equipment item) {
    Objects.requireNonNull(item);
    items.add(item);
  }

  public void unequip(Equipment item) {
    Objects.requireNonNull(item);
    if (!items.remove(item)) {
      throw new IllegalArgumentException("Item not found in inventory");
    }
  }

  @Override
  public Id<EquippedItems> getId() {
    return this.id;
  }
}
