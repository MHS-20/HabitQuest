package habitquest.marketplace.domain.items;

import common.ddd.ValueObject;
import habitquest.marketplace.domain.Level;
import habitquest.marketplace.domain.Money;

public interface Item extends ValueObject {
  String name();

  String description();

  Money price();

  Level requiredLevel();

  Boolean canBuy(Level playerLevel);
}
