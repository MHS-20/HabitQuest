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
        };
    return new ItemResponse(
        type, item.name(), item.description(), item.power(), item.price().amount());
  }

  public static ItemRequest from(Item item) {
    return switch (item) {
      case habitquest.marketplace.domain.items.Weapon w ->
          new ItemRequest("WEAPON", w.name(), w.description(), w.power());
      case habitquest.marketplace.domain.items.Armor a ->
          new ItemRequest("ARMOR", a.name(), a.description(), a.power());
      case habitquest.marketplace.domain.items.HealthPotion hp ->
          new ItemRequest("HEALTH_POTION", hp.name(), hp.description(), hp.power());
      case habitquest.marketplace.domain.items.ManaPotion mp ->
          new ItemRequest("MANA_POTION", mp.name(), mp.description(), mp.power());
    };
  }
}
