package habitquest.avatar.infrastructure.dto;

import habitquest.avatar.domain.items.Armor;
import habitquest.avatar.domain.items.HealthPotion;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.items.ManaPotion;
import habitquest.avatar.domain.items.Weapon;
import habitquest.avatar.infrastructure.AvatarController.*;
import java.util.Locale;

public final class ItemMapper {

  private ItemMapper() {}

  public static Item toDomain(ItemRequest command) {
    if (command.type() == null) {
      throw new IllegalArgumentException("Item type must not be null");
    }
    int power = command.power() != null ? command.power() : 0;
    return switch (command.type().toUpperCase(Locale.getDefault())) {
      case "WEAPON" -> new Weapon(command.name(), command.description(), power);
      case "ARMOR" -> new Armor(command.name(), command.description(), power);
      case "HEALTH_POTION" -> new HealthPotion(command.name(), command.description(), power);
      case "MANA_POTION" -> new ManaPotion(command.name(), command.description(), power);
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
    return new ItemResponse(type, item.name(), item.description(), item.power());
  }
}
