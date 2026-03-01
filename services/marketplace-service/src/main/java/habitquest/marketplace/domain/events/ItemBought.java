package habitquest.marketplace.domain.events;

import habitquest.marketplace.domain.items.Item;

public record ItemBought(Item item) implements MarketplaceEvent {}
