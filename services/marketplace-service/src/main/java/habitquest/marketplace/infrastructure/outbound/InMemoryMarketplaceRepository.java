package habitquest.marketplace.infrastructure.outbound;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.marketplace.application.port.out.MarketplaceRepository;
import habitquest.marketplace.domain.items.Item;
import habitquest.marketplace.domain.items.ItemCatalog;
import habitquest.marketplace.domain.marketplace.Avatar;
import habitquest.marketplace.domain.marketplace.Marketplace;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
@Adapter
public class InMemoryMarketplaceRepository implements MarketplaceRepository {

  private record MarketplaceSnapshot(
      Id<Marketplace> id, Id<Avatar> avatarId, Set<String> soldItemNames) {}

  private final Map<Id<Marketplace>, MarketplaceSnapshot> store = new ConcurrentHashMap<>();
  private final ItemCatalog catalog;

  public InMemoryMarketplaceRepository(ItemCatalog catalog) {
    this.catalog = catalog;
  }

  @Override
  public void save(Marketplace marketplace) {
    Set<String> soldItemNames =
        marketplace.getSoldItems().stream()
            .map(item -> item.name())
            .collect(java.util.stream.Collectors.toSet());
    store.put(
        marketplace.getId(),
        new MarketplaceSnapshot(marketplace.getId(), marketplace.getAvatarId(), soldItemNames));
  }

  @Override
  public Optional<Marketplace> findById(Id<Marketplace> id) {
    return Optional.ofNullable(store.get(id))
        .map(s -> new Marketplace(s.id(), s.avatarId(), catalog, resolveItems(s.soldItemNames())));
  }

  @Override
  public Optional<Marketplace> findByAvatarId(Id<Avatar> avatarId) {
    return store.values().stream()
        .filter(s -> s.avatarId().equals(avatarId))
        .findFirst()
        .map(s -> new Marketplace(s.id(), s.avatarId(), catalog, resolveItems(s.soldItemNames())));
  }

  @Override
  public void deleteById(Id<Marketplace> id) {
    store.remove(id);
  }

  private Set<Item> resolveItems(Set<String> names) {
    return names.stream()
        .map(catalog::getItemByName)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(java.util.stream.Collectors.toSet());
  }
}
