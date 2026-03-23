package habitquest.marketplace.domain;

import common.ddd.Aggregate;
import common.ddd.Id;
import habitquest.marketplace.domain.items.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Marketplace extends Aggregate<Id<Marketplace>> {
  List<Item> getCatalogItems();

  List<Item> getAvailableItemsByType(ItemType type);

  List<Item> getAllAvailableItems();

  Optional<Item> getAvailableItem(String itemName);

  List<Item> getSoldItems();

  Set<String> getSoldItemNames();

  Optional<Item> getSoldItem(String itemName);

  Money buyItem(String itemName);

  Money sellItem(String itemName);

  Id<Avatar> getAvatarId();
}
