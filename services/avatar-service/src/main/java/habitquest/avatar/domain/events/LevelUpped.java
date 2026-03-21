package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.Level;

public record LevelUpped(String avatarId, Level newLevel) implements AvatarEvent {}
