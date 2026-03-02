package habitquest.marketplace.application;

import common.hexagonal.InBoundPort;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.items.*;
import java.util.List;

@InBoundPort
public interface MarketplaceService {
  Marketplace getMarketplace(String marketplaceId);

  List<Item> getItems(String marketplaceId);

  List<Armor> getArmors(String marketplaceId);

  List<Weapon> getWeapons(String marketplaceId);

  List<Potion> getPotions(String marketplaceId);

  List<HealthPotion> getHealthPotions(String marketplaceId);

  List<ManaPotion> getManaPotions(String marketplaceId);

  Item getItemByName(String marketplaceId, String itemName);

  void buyItem(String marketplaceId, String itemName, String avatarId);

  void sellItem(String marketplaceId, String itemName, String avatarId);
}
