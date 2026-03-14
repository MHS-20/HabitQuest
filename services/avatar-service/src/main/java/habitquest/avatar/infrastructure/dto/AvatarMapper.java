package habitquest.avatar.infrastructure.dto;

import habitquest.avatar.domain.avatar.Avatar;
import habitquest.avatar.domain.avatar.AvatarHealth;
import habitquest.avatar.domain.avatar.AvatarMana;
import habitquest.avatar.domain.avatar.Experience;
import habitquest.avatar.domain.avatar.Level;
import habitquest.avatar.domain.avatar.Money;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Inventory;
import habitquest.avatar.domain.spells.Spell;
import habitquest.avatar.domain.stats.AvatarStats;
import habitquest.avatar.infrastructure.AvatarController.*;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class AvatarMapper {

  private AvatarMapper() {}

  public static AvatarResponse toResponse(Avatar avatar) {
    return new AvatarResponse(
        avatar.getId(),
        avatar.getName(),
        toResponse(avatar.getMoney()),
        toResponse(avatar.getLevel()),
        toResponse(avatar.getHealth()),
        toResponse(avatar.getMana()),
        toResponse(avatar.getAvatarStats()),
        toResponse(avatar.getInventory()),
        toResponse(avatar.getEquippedItems()),
        avatar.getSpells().stream().map(Spell::name).toList());
  }

  public static MoneyResponse toResponse(Money money) {
    return new MoneyResponse(money.amount());
  }

  public static ExperienceResponse toResponse(Experience experience) {
    return new ExperienceResponse(experience.amount());
  }

  public static LevelResponse toResponse(Level level) {
    return new LevelResponse(
        level.levelNumber(),
        level.currentExperience().amount(),
        level.experienceRequired().amount());
  }

  public static HealthResponse toResponse(AvatarHealth health) {
    return new HealthResponse(health.current().value(), health.max().value());
  }

  public static ManaResponse toResponse(AvatarMana mana) {
    return new ManaResponse(mana.amount().value(), mana.max().value());
  }

  public static StatsResponse toResponse(AvatarStats stats) {
    return new StatsResponse(
        stats.getStrength().value(), stats.getDefense().value(), stats.getIntelligence().value());
  }

  public static InventoryResponse toResponse(Inventory inventory) {
    List<ItemResponse> items = inventory.getItems().stream().map(ItemMapper::toResponse).toList();
    return new InventoryResponse(inventory.getId(), items);
  }

  public static EquippedItemsResponse toResponse(EquippedItems equippedItems) {
    List<ItemResponse> items =
        equippedItems.getItems().stream().map(ItemMapper::toResponse).toList();
    return new EquippedItemsResponse(equippedItems.getId(), items);
  }
}
