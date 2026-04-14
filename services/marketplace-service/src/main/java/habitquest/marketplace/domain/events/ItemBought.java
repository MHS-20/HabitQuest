package habitquest.marketplace.domain.events;

import common.ddd.Id;
import habitquest.marketplace.domain.marketplace.Avatar;
import habitquest.marketplace.domain.marketplace.Marketplace;

public record ItemBought(Id<Marketplace> marketplaceId, String itemName, Id<Avatar> avatarId)
    implements MarketplaceEvent {}
