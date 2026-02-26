package habitquest.avatar.domain.events;

import habitquest.avatar.domain.items.Item;

public record ItemUnlocked(Item item) implements AvatarEvent {}
