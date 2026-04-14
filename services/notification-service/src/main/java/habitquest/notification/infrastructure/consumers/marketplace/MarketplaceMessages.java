package habitquest.notification.infrastructure.consumers.marketplace;

import java.time.Instant;

public class MarketplaceMessages {
  public record ItemBoughtMessage(
      String marketplaceId, String itemName, String avatarId, Instant occurredOn) {}

  public record ItemSoldMessage(
      String marketplaceId, String itemName, String avatarId, Instant occurredOn) {}
}
