package habitquest.marketplace.infrastructure.dto;

import habitquest.marketplace.domain.items.*;
import habitquest.marketplace.infrastructure.dto.MarketplaceCommands.*;
import habitquest.marketplace.infrastructure.dto.MarketplaceQueries.*;

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

  public static ItemCommand from(Item item) {
    String type =
        switch (item) {
          case Weapon w -> "WEAPON";
          case Armor a -> "ARMOR";
          case HealthPotion h -> "HEALTH_POTION";
          case ManaPotion m -> "MANA_POTION";
        };
    return new ItemCommand(type, item.name(), item.description(), item.power());
  }
}
