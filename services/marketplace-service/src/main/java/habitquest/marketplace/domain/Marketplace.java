package habitquest.marketplace.domain;

import common.ddd.Aggregate;
import habitquest.marketplace.domain.items.*;
import java.util.List;

public interface Marketplace extends Aggregate<String> {
  List<Item> getItems();

  List<Armor> getArmors();

  List<Weapon> getWeapons();

  List<Potion> getPotions();

  List<HealthPotion> getHealthPotions();

  List<ManaPotion> getManaPotions();
}
