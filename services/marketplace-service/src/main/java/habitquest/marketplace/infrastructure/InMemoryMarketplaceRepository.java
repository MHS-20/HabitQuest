package habitquest.marketplace.infrastructure;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.marketplace.application.MarketplaceRepository;
import habitquest.marketplace.domain.Avatar;
import habitquest.marketplace.domain.ItemCatalog;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.MarketplaceImpl;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
@Adapter
public class InMemoryMarketplaceRepository implements MarketplaceRepository {

  private record MarketplaceSnapshot(
      Id<Marketplace> id, Id<Avatar> avatarId, Set<String> soldItems) {}

  private final Map<Id<Marketplace>, MarketplaceSnapshot> store = new ConcurrentHashMap<>();
  private final ItemCatalog catalog;

  public InMemoryMarketplaceRepository(ItemCatalog catalog) {
    this.catalog = catalog;
  }

  @Override
  public void save(Marketplace marketplace) {
    store.put(
        marketplace.getId(),
        new MarketplaceSnapshot(
            marketplace.getId(), marketplace.getAvatarId(), marketplace.getSoldItemNames()));
  }

  @Override
  public Optional<Marketplace> findById(Id<Marketplace> id) {
    return Optional.ofNullable(store.get(id))
        .map(s -> new MarketplaceImpl(s.id(), s.avatarId(), catalog, s.soldItems()));
  }

  @Override
  public Optional<Marketplace> findByAvatarId(Id<Avatar> avatarId) {
    return store.values().stream()
        .filter(s -> s.avatarId().equals(avatarId))
        .findFirst()
        .map(s -> new MarketplaceImpl(s.id(), s.avatarId(), catalog, s.soldItems()));
  }

  @Override
  public void deleteById(Id<Marketplace> id) {
    store.remove(id);
  }
}
