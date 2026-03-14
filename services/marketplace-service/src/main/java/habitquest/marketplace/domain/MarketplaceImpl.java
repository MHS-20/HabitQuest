package habitquest.marketplace.domain;

import habitquest.marketplace.domain.items.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MarketplaceImpl implements Marketplace {
  private final String id;
  private List<Item> items;

  public MarketplaceImpl(String id) {
    this(id, new ArrayList<>());
  }

  public MarketplaceImpl(String id, List<Item> items) {
    this.id = id;
    this.items = items;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public List<Item> getItems(ItemType type) {
    return items.stream().filter(type.filter()).toList();
  }

  @Override
  public Optional<Item> getItem(String itemName) {
    return items.stream().filter(item -> item.name().equals(itemName)).findFirst();
  }

  @Override
  public Money buyItem(String itemName) {
    Optional<Item> itemOpt = getItem(itemName);
    if (itemOpt.isEmpty()) {
      throw new IllegalArgumentException("Item not found: " + itemName);
    }
    Item item = itemOpt.get();
    return item.price();
  }

  @Override
  public Money sellItem(String itemName) {
    Optional<Item> itemOpt = getItem(itemName);
    if (itemOpt.isEmpty()) {
      throw new IllegalArgumentException("Item not found: " + itemName);
    }
    Item item = itemOpt.get();
    return item.price();
  }
}
