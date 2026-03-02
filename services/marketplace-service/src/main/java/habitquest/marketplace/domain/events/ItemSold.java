package habitquest.marketplace.domain.events;

public record ItemSold(String marketplaceId, String itemName, String avatarId)
    implements MarketplaceEvent {}
