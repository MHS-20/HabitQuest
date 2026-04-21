package habitquest.marketplace.application.port.in;

import common.ddd.Id;
import habitquest.marketplace.application.exceptions.MarketplaceNotFoundException;
import habitquest.marketplace.domain.items.Item;
import habitquest.marketplace.domain.items.Level;
import habitquest.marketplace.domain.marketplace.Avatar;
import habitquest.marketplace.domain.marketplace.Marketplace;

public interface MarketplaceCommandService {

  Id<Marketplace> createMarketplaceForAvatar(Id<Avatar> avatarId);

  void buyItem(Id<Marketplace> marketplaceId, Item item, Level currentLevel)
      throws MarketplaceNotFoundException;

  void sellItem(Id<Marketplace> marketplaceId, Item item) throws MarketplaceNotFoundException;
}
