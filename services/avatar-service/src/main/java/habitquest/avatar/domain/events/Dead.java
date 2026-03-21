package habitquest.avatar.domain.events;

import common.ddd.Id;
import habitquest.avatar.domain.avatar.Avatar;

public record Dead(Id<Avatar> avatarId) implements AvatarEvent {}
