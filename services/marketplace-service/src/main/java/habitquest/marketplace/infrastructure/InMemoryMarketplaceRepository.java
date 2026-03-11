package habitquest.marketplace.infrastructure;

import common.hexagonal.Adapter;
import habitquest.marketplace.application.MarketplaceRepository;
import habitquest.marketplace.domain.Marketplace;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
@Adapter
public class InMemoryMarketplaceRepository implements MarketplaceRepository {

  private final Map<String, Marketplace> store = new HashMap<>();

  @Override
  public void save(Marketplace marketplace) {
    store.put(marketplace.getId(), marketplace);
  }

  @Override
  public Optional<Marketplace> findById(String id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public void deleteById(String id) {
    store.remove(id);
  }
}
