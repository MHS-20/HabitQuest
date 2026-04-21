package habitquest.marketplace.application.port.in;

import common.ddd.Id;
import habitquest.marketplace.application.exceptions.MarketplaceNotFoundException;
import habitquest.marketplace.domain.items.Item;
import habitquest.marketplace.domain.items.ItemFilter;
import habitquest.marketplace.domain.items.Level;
import habitquest.marketplace.domain.marketplace.Avatar;
import habitquest.marketplace.domain.marketplace.Marketplace;
import java.util.List;

public interface MarketplaceQueryService {

  Marketplace getMarketplace(Id<Marketplace> marketplaceId) throws MarketplaceNotFoundException;

  Id<Marketplace> getMarketplaceIdByAvatarId(Id<Avatar> avatarId)
      throws MarketplaceNotFoundException;

  Id<Avatar> getAvatarId(Id<Marketplace> marketplaceId) throws MarketplaceNotFoundException;

  List<Item> getAllAvailableItems(Id<Marketplace> marketplaceId)
      throws MarketplaceNotFoundException;

  List<Item> getAvailableItemsByType(Id<Marketplace> marketplaceId, ItemFilter type)
      throws MarketplaceNotFoundException;

  Item getAvailableItem(Id<Marketplace> marketplaceId, Item item)
      throws MarketplaceNotFoundException;

  List<Item> getSoldItems(Id<Marketplace> marketplaceId) throws MarketplaceNotFoundException;

  Item getSoldItem(Id<Marketplace> marketplaceId, Item item) throws MarketplaceNotFoundException;

  boolean canBuyItem(Id<Marketplace> marketplaceId, Item item, Level level)
      throws MarketplaceNotFoundException;
}
