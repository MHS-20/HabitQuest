package habitquest.avatar_service.domain.events;

import habitquest.avatar_service.domain.items.Item;

public record ItemUnlocked(Item item) implements AvatarEvent {}
