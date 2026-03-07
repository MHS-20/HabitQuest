package habitquest.marketplace.domain;

import habitquest.marketplace.domain.items.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MarketplaceImpl implements Marketplace {
  private final String id;
  private List<Item> items;
  private List<Armor> armors;
  private List<Weapon> weapons;
  private List<Potion> potions;
  private List<HealthPotion> healthPotions;
  private List<ManaPotion> manaPotions;

  public MarketplaceImpl(String id) {
    this(
        id,
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>());
  }

  public MarketplaceImpl(
      String id,
      List<Item> items,
      List<Armor> armors,
      List<Weapon> weapons,
      List<Potion> potions,
      List<HealthPotion> healthPotions,
      List<ManaPotion> manaPotions) {
    this.id = id;
    this.items = items;
    this.armors = armors;
    this.weapons = weapons;
    this.potions = potions;
    this.healthPotions = healthPotions;
    this.manaPotions = manaPotions;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public List<Weapon> getWeapons() {
    return weapons;
  }

  @Override
  public List<Item> getItems() {
    return items;
  }

  @Override
  public List<Armor> getArmors() {
    return armors;
  }

  @Override
  public List<HealthPotion> getHealthPotions() {
    return healthPotions;
  }

  @Override
  public List<ManaPotion> getManaPotions() {
    return manaPotions;
  }

  @Override
  public List<Potion> getPotions() {
    return potions;
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
