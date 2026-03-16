package habitquest.marketplace.application;

import common.hexagonal.InBoundPort;
import habitquest.marketplace.domain.ItemNotFoundException;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.items.*;
import java.util.List;

@InBoundPort
public interface MarketplaceService {
  Marketplace getMarketplace(String marketplaceId) throws MarketplaceNotFoundException;

  String createMarketplaceForAvatar(String avatarId);

  List<Item> getAllAvailableItems(String marketplaceId) throws MarketplaceNotFoundException;

  List<Item> getAvailableItemsByType(String marketplaceId, ItemType type)
      throws MarketplaceNotFoundException;

  Item getAvailableItem(String marketplaceId, String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException;

  List<Item> getSoldItems(String marketplaceId) throws MarketplaceNotFoundException;

  Item getSoldItem(String marketplaceId, String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException;

  String getAvatarId(String marketplaceId) throws MarketplaceNotFoundException;

  void buyItem(String marketplaceId, String itemName) throws MarketplaceNotFoundException;

  void sellItem(String marketplaceId, String itemName) throws MarketplaceNotFoundException;
}
