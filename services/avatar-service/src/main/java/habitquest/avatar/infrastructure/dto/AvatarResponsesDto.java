package habitquest.avatar.infrastructure.dto;

import java.time.Instant;
import java.util.List;

public class AvatarResponsesDto {
  public record AvatarCreatedResponse(String id) {}

  public record NameResponse(String name) {}

  public record ItemResponse(String type, String name, String description, Integer power) {}

  public record ExperienceResponse(Integer amount) {}

  public record EquippedItemsResponse(String id, List<ItemResponse> items) {
    public EquippedItemsResponse {
      items = List.copyOf(items);
    }
  }

  public record InventoryResponse(List<ItemResponse> items) {
    public InventoryResponse {
      items = List.copyOf(items);
    }
  }

  public record StatsResponse(Integer strength, Integer defense, Integer intelligence) {}

  public record MoneyResponse(Integer amount) {}

  public record ManaResponse(Integer amount, Integer max) {}

  public record HealthResponse(Integer current, Integer max) {}

  public record DamageResponse(boolean died) {}

  public record InviteResponse(
      String inviteId, String guildId, String guildName, Instant expiresAt) {}

  public record LevelResponse(
      Integer levelNumber, Integer currentExperience, Integer experienceRequired) {}

  public record ErrorResponse(String message) {}

  public record AvatarResponse(
      String id,
      String name,
      MoneyResponse money,
      LevelResponse level,
      HealthResponse health,
      ManaResponse mana,
      StatsResponse stats,
      InventoryResponse inventory,
      EquippedItemsResponse equippedItems,
      List<String> spells) {

    public AvatarResponse {
      spells = spells != null ? List.copyOf(spells) : List.of();
    }
  }
}
