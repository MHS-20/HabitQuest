package habitquest.marketplace.domain;

import habitquest.marketplace.domain.items.*;
import java.util.*;

public class MarketplaceImpl implements Marketplace {
  private final String id;
  private final String avatarId;
  private final ItemCatalog catalog;
  private final Set<String> soldItems;

  public MarketplaceImpl(String id, String avatarId, ItemCatalog catalog) {
    this(id, avatarId, catalog, new HashSet<>());
  }

  public MarketplaceImpl(String id, String avatarId, ItemCatalog catalog, Set<String> soldItems) {
    this.id = id;
    this.avatarId = avatarId;
    this.soldItems = new HashSet<>(soldItems);
    this.catalog = catalog;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getAvatarId() {
    return avatarId;
  }

  public List<Item> getCatalogItems() {
    return this.catalog.getAllItems();
  }

  @Override
  public List<Item> getAllAvailableItems() {
    return catalog.getAllItems().stream().filter(item -> !soldItems.contains(item.name())).toList();
  }

  @Override
  public List<Item> getAvailableItemsByType(ItemType type) {
    return catalog.getItemsByType(type).stream()
        .filter(item -> !soldItems.contains(item.name()))
        .toList();
  }

  @Override
  public List<Item> getSoldItems() {
    return soldItems.stream()
        .map(catalog::getItem)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  @Override
  public Set<String> getSoldItemNames() {
    return soldItems;
  }

  @Override
  public Optional<Item> getAvailableItem(String itemName) {
    if (!catalog.contains(itemName) || soldItems.contains(itemName)) {
      return Optional.empty();
    }
    return catalog.getItem(itemName);
  }

  @Override
  public Optional<Item> getSoldItem(String itemName) {
    if (!soldItems.contains(itemName)) {
      return Optional.empty();
    }
    return catalog.getItem(itemName);
  }

  @Override
  public Money buyItem(String itemName) {
    Item item = catalog.getItem(itemName).orElseThrow(() -> new ItemNotFoundException(itemName));
    if (soldItems.contains(itemName)) {
      throw new IllegalStateException("Item already bought: " + itemName);
    }
    soldItems.add(itemName);
    return item.price();
  }

  @Override
  public Money sellItem(String itemName) {
    Item item = catalog.getItem(itemName).orElseThrow(() -> new ItemNotFoundException(itemName));
    if (!soldItems.remove(itemName)) {
      throw new IllegalArgumentException("Item not sold: " + itemName);
    }
    return item.price();
  }
}
