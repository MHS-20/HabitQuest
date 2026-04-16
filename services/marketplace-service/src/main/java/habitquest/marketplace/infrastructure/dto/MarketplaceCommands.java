package habitquest.marketplace.infrastructure.dto;

import common.cqrs.Command;

public class MarketplaceCommands {
  public record ItemCommand(String type, String name, String description, Integer power)
      implements Command {}

  public record CreateMarketplaceCommand(String avatarId) implements Command {}
}
