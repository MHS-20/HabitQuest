package habitquest.marketplace.infrastructure.dto;

import common.cqrs.Command;

public class MarketplaceCommands {
  public record ItemCommand(
      String type,
      String itemName,
      String description,
      Integer power,
      Integer price,
      Integer requiredLevel)
      implements Command {}

  public record AvatarItemCommand(String type, String name, String description, Integer power)
      implements Command {}

  public record CreateMarketplaceCommand(String avatarId) implements Command {}
}
