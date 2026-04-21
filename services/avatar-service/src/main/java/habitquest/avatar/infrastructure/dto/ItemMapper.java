package habitquest.avatar.infrastructure.dto;

import habitquest.avatar.domain.avatar.Damage;
import habitquest.avatar.domain.avatar.Money;
import habitquest.avatar.domain.items.*;
import habitquest.avatar.infrastructure.dto.AvatarCommands.*;
import habitquest.avatar.infrastructure.dto.AvatarQueries.*;
import java.util.Locale;

public final class ItemMapper {

  private ItemMapper() {}

  public static Money toMoney(MoneyCommand command) {
    if (command.amount() < 0) {
      throw new IllegalArgumentException("Money amount must be non-negative");
    }
    return new Money(command.amount());
  }

  public static Damage toDamage(ApplyDamageCommand command) {
    if (command.amount() < 0) {
      throw new IllegalArgumentException("Damage value must be non-negative");
    }
    return new Damage(command.amount());
  }

  public static HealthPotion toHealthPotion(UsePotionCommand command) {
    int power = command.power() != null ? command.power() : 0;
    return new HealthPotion(command.name(), command.description(), power);
  }

  public static ManaPotion toManaPotion(UsePotionCommand command) {
    int power = command.power() != null ? command.power() : 0;
    return new ManaPotion(command.name(), command.description(), power);
  }

  public static Item toDomain(ItemCommand command) {
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

  public static Equipment toEquipment(ItemCommand command) {
    if (command.type() == null) {
      throw new IllegalArgumentException("Item type must not be null");
    }
    int power = command.power() != null ? command.power() : 0;
    return switch (command.type().toUpperCase(Locale.getDefault())) {
      case "WEAPON" -> new Weapon(command.name(), command.description(), power);
      case "ARMOR" -> new Armor(command.name(), command.description(), power);
      default ->
          throw new IllegalArgumentException("Item type is not equipment: " + command.type());
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
