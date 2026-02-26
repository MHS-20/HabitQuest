package habitquest.marketplace.domain.events;

import common.ddd.DomainEvent;
import habitquest.marketplace.domain.items.Item;

public record ItemSold(Item item) implements DomainEvent {}
