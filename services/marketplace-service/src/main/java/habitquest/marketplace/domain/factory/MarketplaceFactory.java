package habitquest.marketplace.domain.factory;

import common.ddd.Factory;
import common.ddd.Id;
import habitquest.marketplace.Avatar;
import habitquest.marketplace.domain.ItemCatalog;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.MarketplaceImpl;
import org.springframework.stereotype.Component;

@Component
public class MarketplaceFactory implements Factory {
  private final IdGenerator idGenerator;

  public MarketplaceFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Marketplace create(Id<Avatar> avatarId) {
    return new MarketplaceImpl(new Id<>(idGenerator.nextId()), avatarId, new ItemCatalog());
  }
}
