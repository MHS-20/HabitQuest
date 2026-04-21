package habitquest.marketplace.domain.factory;

import common.ddd.Factory;
import common.ddd.Id;
import habitquest.marketplace.domain.items.ItemCatalog;
import habitquest.marketplace.domain.marketplace.Avatar;
import habitquest.marketplace.domain.marketplace.Marketplace;

public class MarketplaceFactory implements Factory {
  private final IdGenerator idGenerator;

  public MarketplaceFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Marketplace create(Id<Avatar> avatarId) {
    return new Marketplace(new Id<>(idGenerator.nextId()), avatarId, new ItemCatalog());
  }
}
