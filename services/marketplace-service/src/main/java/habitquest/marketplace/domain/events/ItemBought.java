package habitquest.marketplace.domain.events;

public record ItemBought(String marketplaceId, String itemName, String avatarId)
    implements MarketplaceEvent {}
