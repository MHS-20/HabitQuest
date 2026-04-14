package habitquest.marketplace.infrastructure.dto;

import habitquest.marketplace.domain.items.*;
import habitquest.marketplace.infrastructure.dto.MarketplaceRequestsDto.*;
import habitquest.marketplace.infrastructure.dto.MarketplaceResponsesDto.*;

public final class ItemMapper {

  private ItemMapper() {}

  public static ItemResponse toResponse(Item item) {
    String type =
        switch (item) {
          case Weapon w -> "WEAPON";
          case Armor a -> "ARMOR";
          case HealthPotion h -> "HEALTH_POTION";
          case ManaPotion m -> "MANA_POTION";
        };
    return new ItemResponse(
        type, item.name(), item.description(), item.power(), item.price().amount());
  }

  public static ItemRequest from(Item item) {
    String type =
        switch (item) {
          case Weapon w -> "WEAPON";
          case Armor a -> "ARMOR";
          case HealthPotion h -> "HEALTH_POTION";
          case ManaPotion m -> "MANA_POTION";
        };
    return new ItemRequest(type, item.name(), item.description(), item.power());
  }
}
