package habitquest.marketplace.application;

import common.hexagonal.InBoundPort;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.items.*;
import java.util.List;

@InBoundPort
public interface MarketplaceService {
  Marketplace getMarketplace(String marketplaceId) throws MarketplaceNotFoundException;

  List<Item> getItems(String marketplaceId, ItemType type) throws MarketplaceNotFoundException;

  Item getSoldItem(String marketplaceId, String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException;

  String getAvatarId(String marketplaceId) throws MarketplaceNotFoundException;

  Item getItemByName(String marketplaceId, String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException;

  void buyItem(String marketplaceId, String itemName) throws MarketplaceNotFoundException;

  void sellItem(String marketplaceId, String itemName) throws MarketplaceNotFoundException;
}
