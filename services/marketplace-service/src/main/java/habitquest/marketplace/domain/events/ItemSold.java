package habitquest.marketplace.domain.events;

import common.ddd.Id;
import habitquest.marketplace.Avatar;
import habitquest.marketplace.domain.Marketplace;

public record ItemSold(Id<Marketplace> marketplaceId, String itemName, Id<Avatar> avatarId)
    implements MarketplaceEvent {}
