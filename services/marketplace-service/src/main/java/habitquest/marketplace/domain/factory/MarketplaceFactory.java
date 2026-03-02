package habitquest.marketplace.domain.factory;

import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.MarketplaceImpl;

public class MarketplaceFactory {
  private final IdGenerator idGenerator;

  public MarketplaceFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Marketplace create(String name) {
    return new MarketplaceImpl(idGenerator.nextId());
  }
}
