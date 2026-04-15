package habitquest.avatar.infrastructure.dto;

import common.cqrs.QueryResponse;
import java.time.Instant;
import java.util.List;

public class AvatarQueries {
  // --- Query Responses ---
  public record NameResponse(String name) implements QueryResponse {}

  public record ItemResponse(String type, String name, String description, Integer power)
      implements QueryResponse {}

  public record ExperienceResponse(Integer amount) implements QueryResponse {}

  public record EquippedItemsResponse(List<ItemResponse> items) implements QueryResponse {
    public EquippedItemsResponse {
      items = List.copyOf(items);
    }
  }

  public record InventoryResponse(List<ItemResponse> items) implements QueryResponse {
    public InventoryResponse {
      items = List.copyOf(items);
    }
  }

  public record StatsResponse(Integer strength, Integer defense, Integer intelligence)
      implements QueryResponse {}

  public record MoneyResponse(Integer amount) implements QueryResponse {}

  public record ManaResponse(Integer amount, Integer max) implements QueryResponse {}

  public record HealthResponse(Integer current, Integer max) implements QueryResponse {}

  public record InviteResponse(String inviteId, String guildId, String guildName, Instant expiresAt)
      implements QueryResponse {}

  public record LevelResponse(
      Integer levelNumber, Integer currentExperience, Integer experienceRequired)
      implements QueryResponse {}

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
      List<String> spells)
      implements QueryResponse {
    public AvatarResponse {
      spells = spells != null ? List.copyOf(spells) : List.of();
    }
  }
}
