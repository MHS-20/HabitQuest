package habitquest.marketplace.domain;

import habitquest.marketplace.domain.items.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MarketplaceImpl implements Marketplace {
  private final String id;
  private final String avatarId;
  private List<Item> items;
  private List<Item> soldItems;

  public MarketplaceImpl(String id, String avatarId) {
    this(id, avatarId, new ArrayList<>());
  }

  public MarketplaceImpl(String id, String avatarId, List<Item> items) {
    this.id = id;
    this.avatarId = avatarId;
    this.items = items;
    this.soldItems = new ArrayList<>();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getAvatarId() {
    return avatarId;
  }

  @Override
  public List<Item> getItems(ItemType type) {
    return items.stream().filter(type.filter()).toList();
  }

  @Override
  public List<Item> getSoldItems() {
    return soldItems;
  }

  @Override
  public Optional<Item> getItem(String itemName) {
    return items.stream().filter(item -> item.name().equals(itemName)).findFirst();
  }

  @Override
  public Optional<Item> getSoldItem(String itemName) {
    return soldItems.stream().filter(item -> item.name().equals(itemName)).findFirst();
  }

  @Override
  public Money buyItem(String itemName) {
    Optional<Item> itemOpt = getItem(itemName);
    if (itemOpt.isEmpty()) {
      throw new IllegalArgumentException("Item not found: " + itemName);
    }
    Item item = itemOpt.get();
    this.items.remove(item);
    this.soldItems.add(item);
    return item.price();
  }

  @Override
  public Money sellItem(String itemName) {
    Optional<Item> itemOpt = getSoldItem(itemName);
    if (itemOpt.isEmpty()) {
      throw new IllegalArgumentException("Item not found: " + itemName);
    }
    Item item = itemOpt.get();
    this.items.add(item);
    this.soldItems.remove(item);
    return item.price();
  }
}
