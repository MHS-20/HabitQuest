package habitquest.avatar.domain.events;

import common.ddd.Id;
import habitquest.avatar.domain.avatar.Avatar;
import habitquest.avatar.domain.stats.AvatarStat;

public record SkillPointAssigned(Id<Avatar> avatarId, AvatarStat stat) implements AvatarEvent {}
