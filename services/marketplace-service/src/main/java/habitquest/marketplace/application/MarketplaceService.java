package habitquest.marketplace.application;

import common.ddd.Id;
import common.hexagonal.InBoundPort;
import habitquest.marketplace.domain.Avatar;
import habitquest.marketplace.domain.ItemNotFoundException;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.items.*;
import java.util.List;

@InBoundPort
public interface MarketplaceService {
  Marketplace getMarketplace(Id<Marketplace> marketplaceId) throws MarketplaceNotFoundException;

  Id<Marketplace> getMarketplaceIdByAvatarId(Id<Avatar> avatarId)
      throws MarketplaceNotFoundException;

  Id<Marketplace> createMarketplaceForAvatar(Id<Avatar> avatarId);

  List<Item> getAllAvailableItems(Id<Marketplace> marketplaceId)
      throws MarketplaceNotFoundException;

  List<Item> getAvailableItemsByType(Id<Marketplace> marketplaceId, ItemType type)
      throws MarketplaceNotFoundException;

  Item getAvailableItem(Id<Marketplace> marketplaceId, String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException;

  List<Item> getSoldItems(Id<Marketplace> marketplaceId) throws MarketplaceNotFoundException;

  Item getSoldItem(Id<Marketplace> marketplaceId, String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException;

  Id<Avatar> getAvatarId(Id<Marketplace> marketplaceId) throws MarketplaceNotFoundException;

  void buyItem(Id<Marketplace> marketplaceId, String itemName, Level currentLevel)
      throws MarketplaceNotFoundException, ItemNotFoundException;

  void sellItem(Id<Marketplace> marketplaceId, String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException;

  boolean canBuyItem(Id<Marketplace> marketplaceId, String itemName, Level level)
      throws MarketplaceNotFoundException;
}
