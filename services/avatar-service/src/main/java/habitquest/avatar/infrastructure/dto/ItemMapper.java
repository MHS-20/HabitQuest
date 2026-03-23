package habitquest.avatar.infrastructure.dto;

import habitquest.avatar.domain.items.Armor;
import habitquest.avatar.domain.items.HealthPotion;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.items.ManaPotion;
import habitquest.avatar.domain.items.Weapon;
import habitquest.avatar.infrastructure.AvatarController.*;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
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
    return new ItemResponse(type, item.name(), item.description(), power);
  }
}
