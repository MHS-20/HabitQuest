package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.Level;

public record LevelUpped(Level newLevel) implements AvatarEvent {}
