package habitquest.marketplace.application;

import common.hexagonal.InBoundPort;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.items.*;
import java.util.List;

@InBoundPort
public interface MarketplaceService {
  Marketplace getMarketplace(String marketplaceId) throws MarketplaceNotFoundException;

  List<Item> getItems(String marketplaceId) throws MarketplaceNotFoundException;

  List<Armor> getArmors(String marketplaceId) throws MarketplaceNotFoundException;

  List<Weapon> getWeapons(String marketplaceId) throws MarketplaceNotFoundException;

  List<Potion> getPotions(String marketplaceId) throws MarketplaceNotFoundException;

  List<HealthPotion> getHealthPotions(String marketplaceId) throws MarketplaceNotFoundException;

  List<ManaPotion> getManaPotions(String marketplaceId) throws MarketplaceNotFoundException;

  Item getItemByName(String marketplaceId, String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException;

  void buyItem(String marketplaceId, String itemName, String avatarId)
      throws MarketplaceNotFoundException;

  void sellItem(String marketplaceId, String itemName, String avatarId)
      throws MarketplaceNotFoundException;
}
