package habitquest.marketplace.domain.events;

import habitquest.marketplace.domain.items.Item;

public record ItemSold(Item item) implements MarketplaceEvent {}
