package habitquest.marketplace.infrastructure;

import common.hexagonal.Adapter;
import habitquest.marketplace.application.MarketplaceRepository;
import habitquest.marketplace.domain.ItemCatalog;
import habitquest.marketplace.domain.Marketplace;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import habitquest.marketplace.domain.MarketplaceImpl;
import org.springframework.stereotype.Component;

@Component
@Adapter
public class InMemoryMarketplaceRepository implements MarketplaceRepository {

  private record MarketplaceSnapshot(String id, String avatarId, Set<String> soldItems) {}

  private final Map<String, MarketplaceSnapshot> store = new ConcurrentHashMap<>();
  private final ItemCatalog catalog;

  public InMemoryMarketplaceRepository(ItemCatalog catalog) {
    this.catalog = catalog;
  }

  @Override
  public void save(Marketplace marketplace) {
    store.put(marketplace.getId(),
            new MarketplaceSnapshot(
                    marketplace.getId(),
                    marketplace.getAvatarId(),
                    marketplace.getSoldItemNames()));
  }

  @Override
  public Optional<Marketplace> findById(String id) {
    return Optional.ofNullable(store.get(id))
            .map(s -> new MarketplaceImpl(s.id(), s.avatarId(), catalog, s.soldItems()));
  }

  @Override
  public void deleteById(String id) {
    store.remove(id);
  }
}
