package habitquest.marketplace.infrastructure.dto;

import habitquest.marketplace.domain.items.*;
import habitquest.marketplace.domain.marketplace.Money;
import habitquest.marketplace.infrastructure.dto.MarketplaceCommands.*;
import habitquest.marketplace.infrastructure.dto.MarketplaceQueries.*;

public final class ItemMapper {

  private ItemMapper() {}

  public static Item toItem(ItemCommand command) {
    return switch (command.type()) {
      case "WEAPON" ->
          new Weapon(
              new BaseItem(
                  command.itemName(),
                  command.description(),
                  command.power(),
                  new Money(command.price()),
                  new Level(command.requiredLevel())));
      case "ARMOR" ->
          new Armor(
              new BaseItem(
                  command.itemName(),
                  command.description(),
                  command.power(),
                  new Money(command.price()),
                  new Level(command.requiredLevel())));
      case "HEALTH_POTION" ->
          new HealthPotion(
              new BaseItem(
                  command.itemName(),
                  command.description(),
                  command.power(),
                  new Money(command.price()),
                  new Level(command.requiredLevel())));
      case "MANA_POTION" ->
          new ManaPotion(
              new BaseItem(
                  command.itemName(),
                  command.description(),
                  command.power(),
                  new Money(command.price()),
                  new Level(command.requiredLevel())));
      default -> throw new IllegalArgumentException("Unknown item type: " + command.type());
    };
  }

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
    return new ItemCommand(
        type,
        item.name(),
        item.description(),
        item.power(),
        item.price().amount(),
        item.requiredLevel().levelNumber());
  }
}
