package habitquest.marketplace.infrastructure.dto;

public class MarketplaceRequestsDto {
  public record ItemRequest(String type, String name, String description, Integer power) {}

  public record CreateMarketplaceRequest(String avatarId) {}
}
