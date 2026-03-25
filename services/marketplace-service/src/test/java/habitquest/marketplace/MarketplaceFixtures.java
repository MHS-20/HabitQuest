package habitquest.marketplace;

import common.ddd.Id;
import habitquest.marketplace.domain.Avatar;
import habitquest.marketplace.domain.ItemCatalog;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.MarketplaceImpl;
import habitquest.marketplace.domain.Money;
import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;
import habitquest.marketplace.domain.items.Armor;
import habitquest.marketplace.domain.items.HealthPotion;
import habitquest.marketplace.domain.items.Level;
import habitquest.marketplace.domain.items.ManaPotion;
import habitquest.marketplace.domain.items.Weapon;

public final class MarketplaceFixtures {

  // Raw string IDs
  public static final String MARKETPLACE_1 = "marketplace-1";
  public static final String MARKETPLACE_2 = "marketplace-2";
  public static final String UNKNOWN_MARKETPLACE = "ghost-99";
  public static final String MISSING_MARKETPLACE = "missing";
  public static final String GHOST_MARKETPLACE = "ghost";

  public static final String AVATAR_1 = "avatar-1";
  public static final String AVATAR_99 = "avatar-99";

  // Typed domain IDs
  public static final Id<Marketplace> MARKETPLACE_ID = new Id<>(MARKETPLACE_1);
  public static final Id<Marketplace> MARKETPLACE_MP_ID = new Id<>(MARKETPLACE_2);
  public static final Id<Marketplace> UNKNOWN_MARKETPLACE_ID = new Id<>(UNKNOWN_MARKETPLACE);
  public static final Id<Marketplace> MISSING_MARKETPLACE_ID = new Id<>(MISSING_MARKETPLACE);
  public static final Id<Marketplace> GHOST_MARKETPLACE_ID = new Id<>(GHOST_MARKETPLACE);
  public static final Id<Avatar> AVATAR_ID = new Id<>(AVATAR_1);
  public static final Id<Avatar> AVATAR_ID_99 = new Id<>(AVATAR_99);

  // Item names
  public static final String SWORD_NAME = "Iron Sword";
  public static final String SHIELD_NAME = "Iron Shield";
  public static final String HEALTH_POTION_NAME = "Health Potion";
  public static final String MANA_POTION_NAME = "Mana Potion";
  public static final String HP_POTION_NAME = "HP Potion";
  public static final String MP_POTION_NAME = "MP Potion";
  public static final String UNKNOWN_ITEM = "Ghost Item";
  public static final String UNKNOWN_ITEM_NAME = "Dragon Blade";

  // Prices
  public static final Money SWORD_PRICE = new Money(50);
  public static final Money SHIELD_PRICE = new Money(40);
  public static final Money HP_PRICE = new Money(10);
  public static final Money MP_PRICE = new Money(10);

  // Levels
  public static final Level LEVEL_1 = new Level(1);
  public static final Level LEVEL_5 = new Level(5);
  public static final Level LEVEL_10 = new Level(10);

  // JSON field-name keys
  public static final String JSON_KEY_MARKETPLACE_ID = "marketplaceId";
  public static final String JSON_KEY_ITEM_NAME = "itemName";
  public static final String JSON_KEY_AVATAR_ID = "avatarId";
  public static final String JSON_KEY_OCCURRED_ON = "occurredOn";

  // Event instances
  public static final ItemBought ITEM_BOUGHT =
      new ItemBought(MARKETPLACE_MP_ID, SWORD_NAME, AVATAR_ID);
  public static final ItemSold ITEM_SOLD = new ItemSold(MARKETPLACE_MP_ID, SWORD_NAME, AVATAR_ID);

  // Item factories
  public static Weapon sword() {
    return new Weapon(SWORD_NAME, "A sturdy iron sword", 30, SWORD_PRICE, LEVEL_1);
  }

  public static Weapon eliteSword() {
    return new Weapon("Elite Sword", "A powerful sword", 80, new Money(200), LEVEL_10);
  }

  public static Weapon midSword() {
    return new Weapon("Mid Sword", "A mid sword", 40, new Money(100), LEVEL_5);
  }

  public static Armor shield() {
    return new Armor(SHIELD_NAME, "A sturdy iron shield", 20, SHIELD_PRICE, LEVEL_1);
  }

  public static HealthPotion healthPotion() {
    return new HealthPotion(HEALTH_POTION_NAME, "Restores health", 50, HP_PRICE, LEVEL_1);
  }

  public static ManaPotion manaPotion() {
    return new ManaPotion(MANA_POTION_NAME, "Restores mana", 50, MP_PRICE, LEVEL_1);
  }

  public static HealthPotion hpPotion() {
    return new HealthPotion(HP_POTION_NAME, "Restores HP", 50, HP_PRICE, LEVEL_1);
  }

  public static ManaPotion mpPotion() {
    return new ManaPotion(MP_POTION_NAME, "Restores MP", 30, MP_PRICE, LEVEL_1);
  }

  // Marketplace factories
  public static Marketplace marketplace(ItemCatalog catalog) {
    return new MarketplaceImpl(MARKETPLACE_ID, AVATAR_ID, catalog);
  }

  // Event factories
  public static ItemBought itemBought() {
    return new ItemBought(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);
  }

  public static ItemBought itemBought(
      Id<Marketplace> marketplaceId, String itemName, Id<Avatar> avatarId) {
    return new ItemBought(marketplaceId, itemName, avatarId);
  }

  public static ItemSold itemSold() {
    return new ItemSold(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);
  }

  public static ItemSold itemSold(
      Id<Marketplace> marketplaceId, String itemName, Id<Avatar> avatarId) {
    return new ItemSold(marketplaceId, itemName, avatarId);
  }

  // Prevent instantiation
  private MarketplaceFixtures() {
    throw new UnsupportedOperationException("utility class");
  }
}
