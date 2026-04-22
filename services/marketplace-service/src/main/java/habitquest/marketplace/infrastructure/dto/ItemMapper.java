package habitquest.marketplace.infrastructure.dto;

import habitquest.marketplace.domain.items.*;
import habitquest.marketplace.domain.marketplace.Money;
import habitquest.marketplace.infrastructure.dto.MarketplaceCommands.*;
import habitquest.marketplace.infrastructure.dto.MarketplaceQueries.*;

public final class ItemMapper {

  private ItemMapper() {}

  public static Item toItem(ItemCommand command) {
    final BaseItem baseItem =
        new BaseItem(
            command.itemName(),
            command.description(),
            command.power(),
            new Money(command.price()),
            new Level(command.requiredLevel()));

    return switch (command.type()) {
      case "WEAPON" -> new Weapon(baseItem);
      case "ARMOR" -> new Armor(baseItem);
      case "HEALTH_POTION" -> new HealthPotion(baseItem);
      case "MANA_POTION" -> new ManaPotion(baseItem);
      default -> throw new IllegalArgumentException("Unknown item type: " + command.type());
    };
  }

  private static String determineType(Item item) {
    return switch (item) {
      case Weapon w -> "WEAPON";
      case Armor a -> "ARMOR";
      case HealthPotion h -> "HEALTH_POTION";
      case ManaPotion m -> "MANA_POTION";
    };
  }

  public static ItemResponse toResponse(Item item) {
    return new ItemResponse(
        determineType(item),
        item.name(),
        item.description(),
        item.power(),
        item.price().amount(),
        item.requiredLevel().levelNumber());
  }

  public static ItemCommand from(Item item) {
    return new ItemCommand(
        determineType(item),
        item.name(),
        item.description(),
        item.power(),
        item.price().amount(),
        item.requiredLevel().levelNumber());
  }

  public static AvatarItemCommand toAvatarItem(Item item) {
    return new AvatarItemCommand(
        determineType(item), item.name(), item.description(), item.power());
  }
}
