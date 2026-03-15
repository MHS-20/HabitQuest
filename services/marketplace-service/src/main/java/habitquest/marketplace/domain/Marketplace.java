package habitquest.marketplace.domain;

import common.ddd.Aggregate;
import habitquest.marketplace.domain.items.*;
import java.util.List;
import java.util.Optional;

public interface Marketplace extends Aggregate<String> {
  List<Item> getItems(ItemType type);

  Optional<Item> getItem(String itemName);

  List<Item> getSoldItems();

  Optional<Item> getSoldItem(String itemName);

  Money buyItem(String itemName);

  Money sellItem(String itemName);

  String getAvatarId();
}
