package habitquest.avatar_service.domain.events;

import habitquest.avatar_service.domain.avatar.Level;

public record LevelUpped(Level newLevel) implements AvatarEvent {}
