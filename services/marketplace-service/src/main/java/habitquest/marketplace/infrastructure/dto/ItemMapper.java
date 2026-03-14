package habitquest.marketplace.infrastructure.dto;

import habitquest.marketplace.domain.items.*;

public final class ItemMapper {

  private ItemMapper() {}

  public static ItemResponse toResponse(Item item) {
    String type =
        switch (item) {
          case Weapon w -> "WEAPON";
          case Armor a -> "ARMOR";
          case HealthPotion hp -> "HEALTH_POTION";
          case ManaPotion mp -> "MANA_POTION";
          default -> "UNKNOWN";
        };
    Integer power =
        switch (item) {
          case Weapon w -> w.attackPower();
          case Armor a -> a.defensePower();
          case HealthPotion hp -> hp.healingPower();
          case ManaPotion mp -> mp.restoringPower();
          default -> null;
        };
    return new ItemResponse(type, item.name(), item.description(), power, item.price().amount());
  }
}
