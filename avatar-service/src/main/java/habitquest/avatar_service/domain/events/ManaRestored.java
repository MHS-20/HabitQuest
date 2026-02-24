package habitquest.avatar_service.domain.events;

import habitquest.avatar_service.domain.avatar.Mana;

public record ManaRestored(Mana mana) implements AvatarEvent {
}
