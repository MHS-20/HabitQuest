package habitquest.marketplace.domain.marketplace;

import common.ddd.Id;
import habitquest.marketplace.domain.exceptions.ItemNotFoundException;
import habitquest.marketplace.domain.items.*;
import java.util.*;

public class Marketplace {
  private final Id<Marketplace> id;
  private final Id<Avatar> avatarId;
  private final ItemCatalog catalog;
  private final Set<Item> soldItems;

  public Marketplace(Id<Marketplace> id, Id<Avatar> avatarId, ItemCatalog catalog) {
    this(id, avatarId, catalog, new HashSet<>());
  }

  public Marketplace(
      Id<Marketplace> id, Id<Avatar> avatarId, ItemCatalog catalog, Set<Item> soldItems) {
    this.id = id;
    this.avatarId = avatarId;
    this.soldItems = new HashSet<>(soldItems);
    this.catalog = catalog;
  }

  public Id<Marketplace> getId() {
    return id;
  }

  public Id<Avatar> getAvatarId() {
    return avatarId;
  }

  public List<Item> getCatalogItems() {
    return Collections.unmodifiableList(catalog.getAllItems());
  }

  public boolean hasItem(Item item) {
    return catalog.contains(item);
  }

  public List<Item> getAllAvailableItems() {
    return catalog.getAllItems().stream().filter(item -> !soldItems.contains(item)).toList();
  }

  public List<Item> getAvailableItemsByType(ItemFilter type) {
    return catalog.getItemsByType(type).stream().filter(item -> !soldItems.contains(item)).toList();
  }

  public List<Item> getSoldItems() {
    return Collections.unmodifiableList(new ArrayList<>(soldItems));
  }

  public Optional<Item> getAvailableItem(Item item) {
    if (!catalog.contains(item) || soldItems.contains(item)) {
      return Optional.empty();
    }
    return catalog.getItem(item);
  }

  public Optional<Item> getSoldItem(Item item) {
    if (!soldItems.contains(item)) {
      return Optional.empty();
    }
    return catalog.getItem(item);
  }

  public Money buyItem(Item item) {
    if (!catalog.contains(item)) {
      throw new ItemNotFoundException(item.name());
    }
    if (soldItems.contains(item)) {
      throw new IllegalStateException("Item already bought: " + item.name());
    }
    soldItems.add(item);
    return item.price();
  }

  public Money sellItem(Item item) {
    if (!catalog.contains(item)) {
      throw new ItemNotFoundException(item.name());
    }
    if (!soldItems.remove(item)) {
      throw new IllegalArgumentException("Item not sold: " + item.name());
    }
    return item.price();
  }
}
