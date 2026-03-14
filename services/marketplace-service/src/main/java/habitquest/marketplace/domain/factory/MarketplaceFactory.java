package habitquest.marketplace.domain.factory;

import common.ddd.Factory;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.MarketplaceImpl;
import org.springframework.stereotype.Component;

@Component
public class MarketplaceFactory implements Factory {
  private final IdGenerator idGenerator;

  public MarketplaceFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Marketplace create() {
    return new MarketplaceImpl(idGenerator.nextId());
  }
}
