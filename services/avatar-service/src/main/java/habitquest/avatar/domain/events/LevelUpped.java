package habitquest.avatar.domain.events;

import common.ddd.Id;
import habitquest.avatar.domain.avatar.Avatar;
import habitquest.avatar.domain.avatar.Level;

public record LevelUpped(Id<Avatar> avatarId, Level newLevel) implements AvatarEvent {}
